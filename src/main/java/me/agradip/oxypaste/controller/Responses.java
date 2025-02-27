package me.agradip.oxypaste.controller;

import java.time.LocalDateTime;
import java.util.UUID;

public class Responses {

    public static class ApiResponse<T> {
        private final boolean success;
        private final T data;
        private final String error;

        public ApiResponse(boolean success, T data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, data, null);
        }

        public static <T> ApiResponse<T> failure(String error) {
            return new ApiResponse<>(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }

        public String getError() {
            return error;
        }
    }

    // Paste-related responses
    public record PasteCreatedResponse(String id, LocalDateTime createdAt, String deletionKey) {}
    public record PasteRetrieveResponse(String id, LocalDateTime createdAt, String content) {}

    // User-related responses
    public record UserCreatedResponse(UUID id, LocalDateTime createdAt) {}
    public record LoginResponse(String sessionToken, LocalDateTime expiresAt) {}

    // Token-related responses
    public record TokenCreatedResponse(String token) {}

    public record TokenViewResponse(String name, LocalDateTime createdAt, LocalDateTime expiresAt) {}

    public record TokenValidationResponse(boolean valid) {}

    public record TokenRevokedResponse(boolean revoked) {}
}
