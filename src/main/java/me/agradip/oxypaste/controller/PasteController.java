package me.agradip.oxypaste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.agradip.oxypaste.config.AppConfig;
import me.agradip.oxypaste.dto.RequestsDto;
import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.exception.ApiException;
import me.agradip.oxypaste.exception.PasteExceptions;
import me.agradip.oxypaste.exception.PasteExceptions.EmptyContentException;
import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.security.AuthRequired;
import me.agradip.oxypaste.service.PasteService;
import me.agradip.oxypaste.service.UserService;
import me.agradip.oxypaste.util.RestUtil;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pastes")
@Tag(name = "Pastes", description = "Endpoints for operating with pastes")
public class PasteController {

    private final PasteService pasteService;
    private final UserService userService;

    @Autowired
    private AppConfig appConfig;

    public PasteController(PasteService pasteService, UserService userService) {
        this.pasteService = pasteService;
        this.userService = userService;
    }

    // Create a new paste
    @PostMapping("/")
    @AuthRequired(strict = false)
    @Operation(
            summary = "Create a new paste",
            description = """
        Creates a new paste.

        - **Authorization is optional.**  
        - If an **Authorization** token is provided, the paste will be associated with the authenticated user.  
        - If no token is provided, the paste is created anonymously.
        """,
            security = @SecurityRequirement(name = "BearerAuthentication")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponsesDto.PasteCreatedResponse.class))),
            @ApiResponse(responseCode = "400",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponsesDto.ErrorResponse.class)))
    })
    public ResponseEntity<?> createPaste(@RequestBody RequestsDto.PasteCreateRequest request) {
        if (request.content() == null || request.content().trim().isEmpty()) throw new EmptyContentException();

        User user = RestUtil.getCurrentUser();
        
        Paste paste = new Paste(request.content(), user);
        if(request.isPublic()) paste.setVisibility(Paste.PasteVisibility.PUBLIC);

        Paste createdPaste = pasteService.createPaste(paste);

        ResponsesDto.PasteCreatedResponse response = new ResponsesDto.PasteCreatedResponse(
                createdPaste.getId()
        );

        return ResponseEntity.ok(response);
    }

    // Retrieve a paste
    @GetMapping("/{id}")
    @Operation(
            summary = "Get paste"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponsesDto.PasteRetrieveResponse.class))),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponsesDto.ErrorResponse.class)))
    })
    public ResponsesDto.PasteRetrieveResponse getPaste(@PathVariable String id) {
        Map<String, String> documentPaths = appConfig.getDocuments();

        if (documentPaths.containsKey(id)) {
            Paste rootPaste = pasteService.getRootDocument(id);
            if (rootPaste != null) {
                return new ResponsesDto.PasteRetrieveResponse(
                        rootPaste.getId(),
                        "root",
                        rootPaste.getCreatedAt(),
                        true,
                        rootPaste.getContent()
                );
            }
        }

        return pasteService.getPaste(id)
                .map(paste -> {
                    return new ResponsesDto.PasteRetrieveResponse(
                            paste.getId(),
                            paste.getUser() == null ? null : paste.getUser().getId().toString(),
                            paste.getCreatedAt(),
                            paste.getVisibility().equals(Paste.PasteVisibility.PUBLIC),
                            paste.getContent()
                    );
                })
                .orElseThrow(() -> new PasteExceptions.PasteNotFound(id));
    }

    // Get public pastes
    @GetMapping("/list/public")
    @Operation(summary = "Get public pastes", description = """
    Retrieves a `Paste Meta` array of all public pastes.

    - This endpoint returns both root documents (if configured) and user-created public pastes.
    - The value for the 'createdBy' property will be 'root' if the paste is a root document.
    - Root documents are predefined in the backend by the sysadmin and always public.
    - User-created pastes are included if they are marked as public.
    
    ### Pagination
    - Supports standard Spring pagination query parameters:
      - `page` (integer, default: 0) - The page index (0-based).
      - `size` (integer) - The number of records per page.
      - `sort` (string, optional) - Sorting criteria in the format `property,asc|desc`. Can be used multiple times.
      
    #### Example Usage:
    - `GET /list/public?page=0&size=10`
    - `GET /list/public?page=1&size=5&sort=createdAt,desc`
    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ResponsesDto.PasteMetaResponse.class)))),
    })
    // todo: fix sorting names
    public List<ResponsesDto.PasteMetaResponse> getPublicPastes(@ParameterObject Pageable pageable) {
        Page<Paste> publicPastesPage = pasteService.getPublicPastes(pageable);

        return publicPastesPage.getContent().stream()
                .map(paste -> new ResponsesDto.PasteMetaResponse(
                        paste.getId(),
                        paste.getUser() == null ? null : paste.getUser().getId().toString(),
                        paste.getCreatedAt(),
                        true
                ))
                .toList();
    }


    // Get root pastes
    @GetMapping("/list/root")
    @Operation(summary = "Get root pastes", description = "Returns an array of `Paste` for all the root pastes")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ResponsesDto.PasteRetrieveResponse.class)))
    )
    public List<ResponsesDto.PasteRetrieveResponse> getRootPastes() {
        return appConfig.getDocuments().keySet().stream()
                .map(s -> {
                    Paste paste = pasteService.getRootDocument(s);
                    return new ResponsesDto.PasteRetrieveResponse(
                            paste.getId(),
                            "root",
                            paste.getCreatedAt(),
                            true,
                            paste.getContent()
                    );
                })
                .toList();
    }


    // Delete paste
    @DeleteMapping("/{id}")
    @AuthRequired
    @Operation(
            summary = "Delete a paste",
            security = @SecurityRequirement(name = "BearerAuthentication"),
            description = """
                    Deletes a paste by its unique ID.
                    
                    **Authorization is required for this action**
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponsesDto.ErrorResponse.class)))
    })
    public ResponseEntity<?> deletePaste(@PathVariable String id) {
        Optional<Paste> optionalPaste = pasteService.getPaste(id);

        if (optionalPaste.isEmpty()) throw new PasteExceptions.PasteNotFound(id);

        Paste paste = optionalPaste.get();
        User user = RestUtil.getCurrentUser();

        if(paste.getUser() != null && !paste.getUser().equals(user)) {
            throw new PasteExceptions.PasteDeleteForbidden(id);
        }

        pasteService.deletePaste(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
