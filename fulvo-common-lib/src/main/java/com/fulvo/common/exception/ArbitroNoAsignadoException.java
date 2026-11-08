package com.fulvo.common.exception;

/**
 * Excepción lanzada cuando un usuario intenta actuar sobre un partido en el cual no está designado como árbitro (RN-08).
 * Mapeada a HTTP 403 Forbidden bajo el estándar RFC 7807 (ProblemDetail).
 */
public class ArbitroNoAsignadoException extends RuntimeException {

    public ArbitroNoAsignadoException(String message) {
        super(message);
    }

    public ArbitroNoAsignadoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArbitroNoAsignadoException(String arbitroDni, Long partidoId) {
        super(String.format("El árbitro con DNI '%s' no está asignado para dirigir el partido con ID '%d'.", arbitroDni, partidoId));
    }
}
