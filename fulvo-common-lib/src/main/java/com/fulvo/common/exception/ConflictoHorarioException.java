package com.fulvo.common.exception;

/**
 * Excepción lanzada cuando se intenta asignar una sede o árbitro que viola la ventana anti-superposición
 * de ±2 horas respecto a otro encuentro ya programado (RN-05 / Subsección 3.1).
 * Mapeada a HTTP 409 Conflict bajo el estándar RFC 7807 (ProblemDetail).
 */
public class ConflictoHorarioException extends ConflictoEstadoException {

    public ConflictoHorarioException(String message) {
        super(message);
    }

    public ConflictoHorarioException(String message, Throwable cause) {
        super(message, cause);
    }
}
