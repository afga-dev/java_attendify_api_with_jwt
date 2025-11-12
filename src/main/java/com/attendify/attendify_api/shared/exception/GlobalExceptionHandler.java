package com.attendify.attendify_api.shared.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage())
                                .findFirst()
                                .orElse("Invalid input data");

                ErrorResponse errorResponse = ErrorResponse.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                message,
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Invalid email or password.",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                String message = "Database integrity constraint was violated";

                if (ex.getMessage().toLowerCase().contains("users_email_key"))
                        message = "The email address is already in use";

                ErrorResponse errorResponse = ErrorResponse.of(
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                message,
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
}
