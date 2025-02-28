package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.config.AppConfig;
import me.agradip.oxypaste.dto.RequestsDto;
import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.security.AuthRequired;
import me.agradip.oxypaste.service.PasteService;
import me.agradip.oxypaste.dto.ResponsesDto.ApiResponse;
import me.agradip.oxypaste.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/paste")
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
    public ResponseEntity<ApiResponse<?>> createPaste(Principal principal, @RequestBody RequestsDto.PasteCreateRequest request) {
        if (request.content() == null || request.content().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Content cannot be empty"));
        }

        User user = null;

        // If the request is authenticated, get the user
        if (principal != null) {
            String username = principal.getName();
            user = userService.getUserByUsername(username).orElse(null);
        }

        Paste paste = new Paste(request.content(), user);
        if(request.isPublic()) paste.setPublic();

        Paste createdPaste = pasteService.createPaste(paste);

        ResponsesDto.PasteCreatedResponse response = new ResponsesDto.PasteCreatedResponse(
                createdPaste.getId(), createdPaste.getCreatedAt(), createdPaste.getDeletionKey()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }



    // Retrieve a paste
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResponsesDto.PasteRetrieveResponse>> getPaste(@PathVariable String id) {
        // Check if the requested ID is a root document
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
                return ResponseEntity.ok(ApiResponse.success(response));
            }
        }

        // Otherwise, fetch regular paste by ID
        return pasteService.getPaste(id)
                .map(paste -> {
                    ResponsesDto.PasteRetrieveResponse response = new ResponsesDto.PasteRetrieveResponse(
                            paste.getId(),
                            paste.getUser() == null ? null : paste.getUser().getId().toString(),
                            paste.getCreatedAt(),
                            paste.isPublic(),
                            paste.getContent()
                    );
                    return ResponseEntity.ok(ApiResponse.success(response));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Paste not found")));
    }


    // Get public pastes (including root documents)
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<ResponsesDto.PasteMetaResponse>>> getPublicPastes() {
        Map<String, String> documentPaths = appConfig.getDocuments();

        // Fetch root documents
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

        // Fetch public pastes
        List<ResponsesDto.PasteMetaResponse> publicPastes = pasteService.getPublicPastes().stream()
                .map(paste -> new ResponsesDto.PasteMetaResponse(
                        paste.getId(),
                        paste.getUser() == null ? null : paste.getUser().getId().toString(),
                        paste.getCreatedAt(),
                        true
                ))
                .toList();

        // Combine both lists
        List<ResponsesDto.PasteMetaResponse> responseList = new ArrayList<>();
        responseList.addAll(rootPastes);
        responseList.addAll(publicPastes);

        return ResponseEntity.ok(ApiResponse.success(responseList));
    }




    // Delete paste (POST way)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePasteByDelete(@PathVariable String id, @RequestParam(name = "key") String deletionKey) {
        return deletePaste(id, deletionKey);
    }

    // Delete paste (GET way)
    @GetMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<?>> deletePasteByGet(@PathVariable String id, @RequestParam(name = "key") String deletionKey) {
        return deletePaste(id, deletionKey);
    }

    private ResponseEntity<ApiResponse<?>> deletePaste(String id, String deletionKey) {
        Optional<Paste> optionalPaste = pasteService.getPaste(id);

        if (optionalPaste.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Paste not found"));
        }

        Paste paste = optionalPaste.get();

        if (!paste.getDeletionKey().equals(deletionKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure("Invalid deletion key"));
        }

        pasteService.deletePaste(id);

        return ResponseEntity.ok(ApiResponse.success("Paste deleted successfully"));
    }
}
