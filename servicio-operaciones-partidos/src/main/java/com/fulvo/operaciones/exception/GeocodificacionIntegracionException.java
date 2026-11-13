package com.fulvo.operaciones.exception;

import org.springframework.http.HttpStatus;

public class GeocodificacionIntegracionException extends RuntimeException {

    private final HttpStatus status;

    public GeocodificacionIntegracionException(
            String message,
            HttpStatus status,
            Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}