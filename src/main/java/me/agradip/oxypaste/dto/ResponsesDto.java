package me.agradip.oxypaste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import me.agradip.oxypaste.model.Paste;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ResponsesDto {
    // Paste-related responses
    @Schema(name = "Paste Created", description = "Response when a paste is successfully created")
    public record PasteCreatedResponse(
            @Schema(description = "The unique ID of the created paste")
            String id
    ) {}

    @Schema(name = "Paste Metadata")
    public record PasteMetaResponse(
            @Schema(description = "The unique ID of the paste")
            String id,

            @Schema(description = "The title of the paste", nullable = true)
            String title,

            @Schema(description = "UUID of the user who created the paste. 'root' is used instead if the paste is a root document.")
            String createdBy,

            @Schema(description = "Timestamp of when the paste was created")
            LocalDateTime createdAt,

            @JsonProperty("public")
            @Schema(description = "Indicates if the paste is public or private")
            boolean isPublic,

            @Schema(description = "Programming Language")
            Paste.Language language
    ) {}

    @Schema(name = "Paste")
    public record PasteRetrieveResponse(
            @Schema(description = "The unique ID of the paste")
            String id,

            @Schema(description = "The title of the paste")
            String title,

            @Schema(description = "Username of the creator of the paste")
            String createdBy,

            @Schema(description = "Timestamp of when the paste was created")
            LocalDateTime createdAt,

            @JsonProperty("public")
            @Schema(description = "Indicates the visibility of the paste")
            boolean isPublic,

            @Schema(description = "The actual content of the paste")
            String content,

            @Schema(description = "The programming language of the paste")
            Paste.Language language
    ) {}

    // User-related responses
    public record UserCreateAcceptedResponse(String token) {}
    public record UserCreatedResponse(UUID id, Instant createdAt) {}
    public record LoginResponse(String sessionToken, Instant expiresAt) {}
    public record LoggedUserInfoResponse(UUID id, String username, String email, Instant createdAt) {}

    // Token-related responses
    public record TokenCreatedResponse(UUID id, String token) {}
    public record TokenViewResponse(UUID id, String name, Instant createdAt, Instant expiresAt) {}
    public record TokenValidationResponse(boolean valid) {}
    public record TokenRevokedResponse(boolean revoked) {}

    public record ErrorResponse(@Schema(description = "Message that describes the error") String error) {}

    public record StatisticsResponse(long users, long pastes) {}
    public record VersionResponse(String version) {}

    public record RecaptchaResponseDto(
        boolean success,

        @JsonProperty("error-codes")
        List<String> errorCodes,

        String hostname,

        @JsonProperty("challenge_ts")
        String challengeTs) {}
}
