package com.fulvo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO para la consulta y resolución de coordenadas geográficas de sedes (RN-06)
 * entre servicio-operaciones-partidos y servicio-geocodificacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeocodificacionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Dirección textual a geocodificar (ej: "Av. Colón 1500, Córdoba, Argentina").
     */
    private String direccion;

    /**
     * Latitud resuelta o ingresada manualmente como fallback.
     */
    private Double latitud;

    /**
     * Longitud resuelta o ingresada manualmente como fallback.
     */
    private Double longitud;

    /**
     * Origen de las coordenadas obtenidas: "NOMINATIM", "MANUAL", "WIREMOCK".
     */
    private String fuente;

    /**
     * Nombre legible o descripción normalizada devuelta por el proveedor satelital.
     */
    private String displayNombre;
}
