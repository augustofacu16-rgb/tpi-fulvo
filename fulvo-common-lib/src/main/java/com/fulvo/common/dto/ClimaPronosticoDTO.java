package com.fulvo.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la consulta meteorológica y evaluación de severidad climática a 24 horas (RN-07)
 * entre servicio-operaciones-partidos y servicio-clima.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClimaPronosticoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Latitud de la sede del encuentro.
     */
    private Double latitud;

    /**
     * Longitud de la sede del encuentro.
     */
    private Double longitud;

    /**
     * Fecha y hora para la cual se solicita el pronóstico.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;

    /**
     * Temperatura estimada en grados Celsius.
     */
    private Double temperatura;

    /**
     * Precipitación acumulada estimada en milímetros (mm).
     */
    private Double precipitacionMm;

    /**
     * Código meteorológico WMO (World Meteorological Organization).
     */
    private Integer codigoClima;

    /**
     * Indicador de severidad climática según RN-07:
     * true si precipitacionMm > 40 o códigos meteorológicos severos (tormenta fuerte / granizo).
     */
    private Boolean condicionAdversa;

    /**
     * Descripción textual legible de la condición atmosférica prevista.
     */
    private String descripcion;
}
