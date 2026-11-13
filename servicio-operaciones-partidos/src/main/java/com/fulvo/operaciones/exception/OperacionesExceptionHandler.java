package com.fulvo.operaciones.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.sql.SQLException;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
@Order(0)
public class OperacionesExceptionHandler {

    @ExceptionHandler(GeocodificacionIntegracionException.class)
    public ResponseEntity<ProblemDetail> handleGeocodificacion(
            GeocodificacionIntegracionException ex,
            HttpServletRequest request) {

        boolean is422 = ex.getStatus().value() == HttpStatus.UNPROCESSABLE_ENTITY.value();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                ex.getStatus(),
                ex.getMessage()
        );

        problem.setTitle(
                is422
                        ? "Dirección no encontrada"
                        : "Servicio de geocodificación no disponible"
        );

        problem.setType(URI.create(
                is422
                        ? "https://fulvo.com/errors/direccion-no-encontrada"
                        : "https://fulvo.com/errors/servicio-externo"
        ));

        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());

        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return ResponseEntity
                .status(ex.getStatus())
                .body(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        if (isDuplicateKeyViolation(ex)) {
            log.warn("Violación de clave única / duplicada en operaciones [{}]: {}",
                    request.getRequestURI(), ex.getMessage());

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT,
                    "Conflicto de clave duplicada: el registro ya existe"
            );
            problem.setTitle("Conflicto de Estado");
            problem.setType(URI.create("https://fulvo.com/errors/conflicto-estado"));
            problem.setInstance(URI.create(request.getRequestURI()));
            problem.setProperty("timestamp", Instant.now().toString());

            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                problem.setProperty("correlationId", correlationId);
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
        }

        // Si no es un conflicto por clave duplicada, no se convierte en 409
        throw ex;
    }

    private boolean isDuplicateKeyViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlEx) {
                if ("23505".equals(sqlEx.getSQLState())) {
                    return true;
                }
            }
            if (current instanceof ConstraintViolationException cve) {
                if ("23505".equals(cve.getSQLState())) {
                    return true;
                }
                String constraint = cve.getConstraintName();
                if (constraint != null && (constraint.contains("arbitro_pkey") || constraint.toLowerCase().contains("arbitro"))) {
                    return true;
                }
            }
            String msg = current.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("duplicate key") || lower.contains("unique constraint") || lower.contains("violates unique")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}