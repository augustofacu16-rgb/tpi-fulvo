package com.fulvo.common.exception;

/**
 * Excepción lanzada cuando una entidad o recurso solicitado no existe en el sistema.
 * Mapeada a HTTP 404 Not Found bajo el estándar RFC 7807 (ProblemDetail).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }

    public RecursoNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(String.format("El recurso '%s' con identificador '%s' no fue encontrado.", recurso, id));
    }
}
