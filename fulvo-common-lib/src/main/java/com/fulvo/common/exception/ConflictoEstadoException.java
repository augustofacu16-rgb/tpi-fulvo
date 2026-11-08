package com.fulvo.common.exception;

/**
 * Excepción lanzada cuando una operación intenta una transición inválida en las máquinas de estado
 * o se produce una colisión de concurrencia o recursos (cupo lleno, solapamiento, etc.).
 * Mapeada a HTTP 409 Conflict bajo el estándar RFC 7807 (ProblemDetail).
 */
public class ConflictoEstadoException extends RuntimeException {

    public ConflictoEstadoException(String message) {
        super(message);
    }

    public ConflictoEstadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
