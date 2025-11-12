package com.attendify.attendify_api.shared.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestap,
        int status,
        String error,
        String message,
        String path) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path);
    }
}
