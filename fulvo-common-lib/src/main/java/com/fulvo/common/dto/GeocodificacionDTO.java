package com.fulvo.common.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "La dirección de la sede es obligatoria")
    @Size(min = 5, max = 250, message = "La dirección debe tener entre 5 y 250 caracteres")
    private String direccion;

    /**
     * Latitud resuelta o ingresada manualmente como fallback.
     */
    @DecimalMin(value = "-90.0", message = "La latitud mínima es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud máxima es 90.0")
    private Double latitud;

    /**
     * Longitud resuelta o ingresada manualmente como fallback.
     */
    @DecimalMin(value = "-180.0", message = "La longitud mínima es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud máxima es 180.0")
    private Double longitud;

    /**
     * Origen de las coordenadas obtenidas: "NOMINATIM" o "MANUAL" (RN-06).
     */
    @Pattern(regexp = "^(NOMINATIM|MANUAL)$", message = "La fuente debe ser NOMINATIM o MANUAL")
    private String fuente;

    /**
     * Nombre legible o descripción normalizada devuelta por el proveedor satelital.
     */
    private String displayNombre;
}
