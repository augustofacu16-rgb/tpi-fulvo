package com.fulvo.operaciones.exception;

public class TorneosIntegracionException extends RuntimeException {

    public TorneosIntegracionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
