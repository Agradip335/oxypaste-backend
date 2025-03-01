package me.agradip.oxypaste.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResponsesDto {
    // Paste-related responses
    @Schema(name = "Paste Created", description = "Response when a paste is successfully created")
    public record PasteCreatedResponse(String id, LocalDateTime createdAt, String deletionKey) {}
    public record PasteMetaResponse(String id, String createdBy, LocalDateTime createdAt, boolean isPublic) {}
    public record PasteRetrieveResponse(String id, String createdBy, LocalDateTime createdAt, boolean isPublic, String content) {}

    // User-related responses
    public record UserCreatedResponse(UUID id, LocalDateTime createdAt) {}
    public record LoginResponse(String sessionToken, LocalDateTime expiresAt) {}

    // Token-related responses
    public record TokenCreatedResponse(String token) {}

    public record TokenViewResponse(String name, LocalDateTime createdAt, LocalDateTime expiresAt) {}

    public record TokenValidationResponse(boolean valid) {}

    public record TokenRevokedResponse(boolean revoked) {}

    public record ErrorResponse(String error) {}
}
