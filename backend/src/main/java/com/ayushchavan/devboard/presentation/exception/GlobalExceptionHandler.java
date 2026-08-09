package com.ayushchavan.devboard.presentation.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ayushchavan.devboard.application.exception.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(
            AuthenticationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        String message = exception.getMessage();

        if (isForbiddenMessage(message)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", message));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    private boolean isForbiddenMessage(String message) {
        return "Insufficient workspace permissions".equals(message)
                || "User is not a member of this workspace".equals(message)
                || "Workspace owner cannot be removed".equals(message)
                || "Team does not belong to this workspace".equals(message);
    }
}
