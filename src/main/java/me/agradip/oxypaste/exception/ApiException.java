package me.agradip.oxypaste.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ApiException(int status, String message) {
        super(message);
        this.status = HttpStatus.resolve(status);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
