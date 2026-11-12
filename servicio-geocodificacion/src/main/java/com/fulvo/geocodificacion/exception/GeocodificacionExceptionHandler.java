package com.fulvo.geocodificacion.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GeocodificacionExceptionHandler {

    @ExceptionHandler(GeocodificacionNoDisponibleException.class)
    public ResponseEntity<ProblemDetail> handleGeocodificacion(
            GeocodificacionNoDisponibleException exception,
            HttpServletRequest request) {

        log.warn("No se pudo resolver la ubicación de la sede", exception);

        ProblemDetail problem = ProblemDetail.forStatus(422);
        problem.setTitle("Geocodificación no disponible");
        problem.setDetail(exception.getMessage());
        problem.setType(URI.create(
                "https://fulvo.com/errors/geocodificacion-no-disponible"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());

        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return ResponseEntity.status(422).body(problem);
    }
}