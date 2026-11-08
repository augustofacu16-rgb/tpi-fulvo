package com.fulvo.common.exception;

import com.fulvo.common.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global centralizado de excepciones bajo el estándar RFC 7807 (ProblemDetail) (Apuntes 18 y 23).
 * Intercepta excepciones de dominio y validación, retornando respuestas estructuradas enriquecidas
 * con timestamp, correlationId y URI de la petición.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final String TYPE_BASE = "https://fulvo.com/errors/";

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> handleRecursoNoEncontrado(RecursoNoEncontradoException ex,
                                                                   HttpServletRequest request) {
        log.warn("Recurso no encontrado [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, "Recurso No Encontrado", ex.getMessage(), "not-found", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ConflictoHorarioException.class)
    public ResponseEntity<ProblemDetail> handleConflictoHorario(ConflictoHorarioException ex,
                                                               HttpServletRequest request) {
        log.warn("Conflicto horario anti-superposicion [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.CONFLICT, "Conflicto Horario", ex.getMessage(), "conflicto-horario", request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(ConflictoEstadoException.class)
    public ResponseEntity<ProblemDetail> handleConflictoEstado(ConflictoEstadoException ex,
                                                              HttpServletRequest request) {
        log.warn("Conflicto de estado o regla concurrente [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.CONFLICT, "Conflicto de Estado", ex.getMessage(), "conflicto-estado", request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(ArbitroNoAsignadoException.class)
    public ResponseEntity<ProblemDetail> handleArbitroNoAsignado(ArbitroNoAsignadoException ex,
                                                                HttpServletRequest request) {
        log.warn("Acceso denegado a arbitro no asignado [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.FORBIDDEN, "Arbitro No Autorizado", ex.getMessage(), "arbitro-no-asignado", request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException ex,
                                                           HttpServletRequest request) {
        log.warn("Violacion de regla de negocio [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Regla de Negocio Incumplida", ex.getMessage(), "regla-negocio", request);
        if (ex.getCodigoRegla() != null) {
            problem.setProperty("codigoRegla", ex.getCodigoRegla());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        log.warn("Fallo de validacion en peticion [{}]: {} errores de campo", request.getRequestURI(), ex.getBindingResult().getErrorCount());
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validacion Fallida", "La peticion contiene campos invalidos o incompletos.", "validacion", request);

        Map<String, String> invalidParams = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            invalidParams.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("invalidParams", invalidParams);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ProblemDetail> handleIllegalArgument(RuntimeException ex,
                                                              HttpServletRequest request) {
        log.warn("Argumento o estado ilegal [{}]: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Peticion Invalida", ex.getMessage(), "peticion-invalida", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex,
                                                                HttpServletRequest request) {
        log.error("Error no controlado al procesar peticion [{}]", request.getRequestURI(), ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno del Servidor", "Ocurrio un error inesperado al procesar la solicitud.", "error-interno", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status,
                                             String title,
                                             String detail,
                                             String typeSuffix,
                                             HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(TYPE_BASE + typeSuffix));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());

        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        if (correlationId != null) {
            problem.setProperty(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, correlationId);
        }

        return problem;
    }
}
