package me.agradip.oxypaste.advice;

import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice(basePackages = "me.agradip.oxypaste.controller")
public class GlobalExceptionHandlerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponsesDto.ApiResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ResponsesDto.ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponsesDto.ApiResponse> handleUnexpectedException(Exception ex) {
        ex.printStackTrace(); // TODO: Replace with logger in production
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponsesDto.ApiResponse.failure("An unexpected error took place"));
    }
}
