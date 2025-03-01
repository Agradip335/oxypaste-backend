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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/paste")
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
    @PostMapping
    @AuthRequired(strict = false)
    @Operation(summary = "Create paste", description = "Add a new paste")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ResponsesDto.PasteCreatedResponse.class))),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = ResponsesDto.ErrorResponse.class)))
    })
    public ResponseEntity<?> createPaste(Principal principal, @RequestBody RequestsDto.PasteCreateRequest request) {
        if (request.content() == null || request.content().trim().isEmpty()) throw new EmptyContentException();

        User user = RestUtil.getCurrentUser();
        
        Paste paste = new Paste(request.content(), user);
        if (request.isPublic()) paste.setPublic();

        Paste createdPaste = pasteService.createPaste(paste);

        ResponsesDto.PasteCreatedResponse response = new ResponsesDto.PasteCreatedResponse(
                createdPaste.getId()
        );

        return ResponseEntity.ok(response);
    }

    // Retrieve a paste
    @GetMapping("/{id}")
    @Operation(
            summary = "Get paste meta"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResponsesDto.PasteRetrieveResponse.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ResponsesDto.ErrorResponse.class)))
    })
    public ResponsesDto.PasteRetrieveResponse getPaste(@PathVariable String id) {
        Map<String, String> documentPaths = appConfig.getDocuments();

        if (documentPaths.containsKey(id)) {
            Paste rootPaste = pasteService.getRootDocument(id);
            if (rootPaste != null) {
                ResponsesDto.PasteRetrieveResponse response = new ResponsesDto.PasteRetrieveResponse(
                        rootPaste.getId(),
                        "root",
                        rootPaste.getCreatedAt(),
                        true,
                        rootPaste.getContent()
                );
                return response;
            }
        }

        return pasteService.getPaste(id)
                .map(paste -> {
                    ResponsesDto.PasteRetrieveResponse response = new ResponsesDto.PasteRetrieveResponse(
                            paste.getId(),
                            paste.getUser() == null ? null : paste.getUser().getId().toString(),
                            paste.getCreatedAt(),
                            paste.isPublic(),
                            paste.getContent()
                    );
                    return response;
                })
                .orElseThrow(() -> new PasteExceptions.PasteNotFound(id));
    }

    // Get public pastes
    @GetMapping("/list")
    @Operation(summary = "Get public pastes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponsesDto.PasteMetaResponse.class)))),
    })
    public List<ResponsesDto.PasteMetaResponse> getPublicPastes() {
        Map<String, String> documentPaths = appConfig.getDocuments();

        List<ResponsesDto.PasteMetaResponse> rootPastes = documentPaths.keySet().stream()
                .map(s -> {
                    Paste paste = pasteService.getRootDocument(s);
                    return new ResponsesDto.PasteMetaResponse(
                            paste.getId(),
                            "root",
                            paste.getCreatedAt(),
                            true
                    );
                })
                .toList();

        List<ResponsesDto.PasteMetaResponse> publicPastes = pasteService.getPublicPastes().stream()
                .map(paste -> new ResponsesDto.PasteMetaResponse(
                        paste.getId(),
                        paste.getUser() == null ? null : paste.getUser().getId().toString(),
                        paste.getCreatedAt(),
                        true
                ))
                .toList();

        List<ResponsesDto.PasteMetaResponse> responseList = new ArrayList<>();
        responseList.addAll(rootPastes);
        responseList.addAll(publicPastes);

        return responseList;
    }

    // Delete paste
    @DeleteMapping("/{id}")
    @AuthRequired
    @Operation(summary = "Delete a paste", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403")
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

//        return ResponseEntity.ok().header("content-type:application/json").body("{}");
        return ResponseEntity.ok().build();
    }
}
