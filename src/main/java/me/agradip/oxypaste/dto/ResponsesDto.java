package me.agradip.oxypaste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResponsesDto {
    // Paste-related responses
    public record PasteCreatedResponse(
            @Schema(description = "The unique ID of the created paste")
            String id
    ) {}

    public record PasteMetaResponse(
            @Schema(description = "The unique ID of the paste")
            String id,

            @Schema(description = "Username of the creator of the paste")
            String createdBy,

            @Schema(description = "Timestamp of when the paste was created")
            LocalDateTime createdAt,

            @JsonProperty("public")
            @Schema(description = "Indicates if the paste is public or private")
            boolean isPublic
    ) {}

    public record PasteRetrieveResponse(
            @Schema(description = "The unique ID of the paste")
            String id,

            @Schema(description = "Username of the creator of the paste")
            String createdBy,

            @Schema(description = "Timestamp of when the paste was created")
            LocalDateTime createdAt,

            @JsonProperty("public")
            @Schema(description = "Indicates if the paste is public or private")
            boolean isPublic,

            @Schema(description = "The actual content of the paste")
            String content
    ) {}

    // User-related responses
    public record UserCreatedResponse(UUID id, LocalDateTime createdAt) {}
    public record LoginResponse(String sessionToken, LocalDateTime expiresAt) {}

    // Token-related responses
    public record TokenCreatedResponse(String token) {}

    public record TokenViewResponse(String name, LocalDateTime createdAt, LocalDateTime expiresAt) {}

    public record TokenValidationResponse(boolean valid) {}

    public record TokenRevokedResponse(boolean revoked) {}

    public record ErrorResponse(@Schema(description = "Message that describes the error") String error) {}
}
