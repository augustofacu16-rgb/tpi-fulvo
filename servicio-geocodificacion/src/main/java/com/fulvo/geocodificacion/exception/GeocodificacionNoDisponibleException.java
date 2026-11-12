package com.fulvo.geocodificacion.exception;

public class GeocodificacionNoDisponibleException extends RuntimeException {

    public GeocodificacionNoDisponibleException(
            String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}