package com.fulvo.common.exception;

/**
 * Excepción lanzada cuando un partido de formato Llaves culmina en empate y no se informa el desempate por penales (RN-10).
 * Mapeada a HTTP 400 Bad Request bajo el estándar RFC 7807 (ProblemDetail).
 */
public class EmpateNoPermitidoException extends BusinessRuleException {

    public EmpateNoPermitidoException(String message) {
        super(message, "RN-10");
    }

    public EmpateNoPermitidoException(String message, Throwable cause) {
        super(message, "RN-10", cause);
    }
}
