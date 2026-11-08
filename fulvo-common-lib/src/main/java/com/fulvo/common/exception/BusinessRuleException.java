package com.fulvo.common.exception;

import lombok.Getter;

/**
 * Excepción de dominio lanzada ante la violación de cualquier regla de negocio oficial (RN-01 a RN-12).
 * Mapeada a HTTP 400 Bad Request (o 422 Unprocessable Entity según corresponda).
 */
@Getter
public class BusinessRuleException extends RuntimeException {

    private final String codigoRegla;

    public BusinessRuleException(String message) {
        super(message);
        this.codigoRegla = null;
    }

    public BusinessRuleException(String message, String codigoRegla) {
        super(message);
        this.codigoRegla = codigoRegla;
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
        this.codigoRegla = null;
    }

    public BusinessRuleException(String message, String codigoRegla, Throwable cause) {
        super(message, cause);
        this.codigoRegla = codigoRegla;
    }
}
