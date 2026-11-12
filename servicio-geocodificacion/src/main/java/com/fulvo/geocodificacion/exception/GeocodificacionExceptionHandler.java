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

        log.warn("El servicio de geocodificación no está disponible", exception);

        ProblemDetail problem = ProblemDetail.forStatus(503);
        problem.setTitle("Servicio de geocodificación no disponible");
        problem.setDetail(exception.getMessage());
        problem.setType(URI.create(
                "https://fulvo.com/errors/geocodificacion-no-disponible"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());

        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return ResponseEntity.status(503).body(problem);
    }

    @ExceptionHandler(DireccionNoEncontradaException.class)
    public ResponseEntity<ProblemDetail> handleDireccionNoEncontrada(
            DireccionNoEncontradaException exception,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(422);
        problem.setTitle("Dirección no encontrada");
        problem.setDetail(exception.getMessage());
        problem.setType(URI.create(
                "https://fulvo.com/errors/direccion-no-encontrada"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());

        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return ResponseEntity.status(422).body(problem);
    }
}
