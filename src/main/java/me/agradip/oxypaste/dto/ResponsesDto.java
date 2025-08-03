package me.agradip.oxypaste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import me.agradip.oxypaste.model.Paste;
import org.springframework.boot.jackson.JsonComponent;

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
            boolean isPublic
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
            String content
    ) {}

    // User-related responses
    public record UserCreateLinkSentResponse(UUID id, String email) {}
    public record UserCreatedResponse(UUID id, LocalDateTime createdAt) {}
    public record LoginResponse(String sessionToken, LocalDateTime expiresAt) {}
    public record LoggedUserInfoResponse(UUID id, String username, String email, LocalDateTime createdAt) {}

    // Token-related responses
    public record TokenCreatedResponse(String token) {}

    public record TokenViewResponse(String name, LocalDateTime createdAt, LocalDateTime expiresAt) {}

    public record TokenValidationResponse(boolean valid) {}

    public record TokenRevokedResponse(boolean revoked) {}

    public record ErrorResponse(@Schema(description = "Message that describes the error") String error) {}

    public record RecaptchaResponseDto(
        boolean success,

        @JsonProperty("error-codes")
        List<String> errorCodes,

        String hostname,

        @JsonProperty("challenge_ts")
        String challengeTs) {}
}
