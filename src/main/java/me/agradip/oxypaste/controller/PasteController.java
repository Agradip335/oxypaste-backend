package me.agradip.oxypaste.controller;

import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.service.PasteService;
import me.agradip.oxypaste.controller.Responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/paste")
public class PasteController {

    private final PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    // Create a new paste
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPaste(@RequestBody String content) {
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Content cannot be empty"));
        }

        Paste createdPaste = pasteService.createPaste(content);
        Responses.PasteCreatedResponse response = new Responses.PasteCreatedResponse(
                createdPaste.getId(), createdPaste.getCreatedAt(), createdPaste.getDeletionKey()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Retrieve a paste
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Responses.PasteRetrieveResponse>> getPaste(@PathVariable String id) {
        return pasteService.getPaste(id)
                .map(paste -> {
                    Responses.PasteRetrieveResponse response = new Responses.PasteRetrieveResponse(
                            paste.getId(), paste.getCreatedAt(), paste.getContent()
                    );
                    return ResponseEntity.ok(ApiResponse.success(response));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Paste not found")));
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
