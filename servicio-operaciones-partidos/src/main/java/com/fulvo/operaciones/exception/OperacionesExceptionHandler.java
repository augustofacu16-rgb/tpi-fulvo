package com.fulvo.operaciones.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@Order(0)
public class OperacionesExceptionHandler {

    @ExceptionHandler(GeocodificacionIntegracionException.class)
    public ResponseEntity<ProblemDetail> handleGeocodificacion(
            GeocodificacionIntegracionException ex,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                ex.getStatus(),
                ex.getMessage()
        );

        problem.setTitle(
                ex.getStatus().value() == 422
                        ? "Geocodificación no disponible"
                        : "Error en servicio externo"
        );

        problem.setType(URI.create(
                ex.getStatus().value() == 422
                        ? "https://fulvo.com/errors/geocodificacion-no-disponible"
                        : "https://fulvo.com/errors/servicio-externo"
        ));

        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return ResponseEntity
                .status(ex.getStatus())
                .body(problem);
    }
}