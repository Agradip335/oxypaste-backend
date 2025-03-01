package me.agradip.oxypaste.exception;

import org.springframework.http.HttpStatus;

public class PasteExceptions {
    public static class EmptyContentException extends ApiException {
        public EmptyContentException() {
            super(HttpStatus.BAD_REQUEST, "Content cannot be empty");
        }
    }

    public static class PasteNotFound extends ApiException {
        public PasteNotFound(String id) {
            super(HttpStatus.NOT_FOUND, "Paste not found: " + id);
        }
    }

    public static class PasteDeleteForbidden extends ApiException {
        public PasteDeleteForbidden(String id) {
            super(HttpStatus.FORBIDDEN, "You do not have the permission to delete the paste " + id);
        }
    }
}
