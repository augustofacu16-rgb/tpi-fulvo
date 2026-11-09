package com.fulvo.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
    @NotNull(message = "La latitud de la sede es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud mínima es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud máxima es 90.0")
    private Double latitud;

    /**
     * Longitud de la sede del encuentro.
     */
    @NotNull(message = "La longitud de la sede es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud mínima es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud máxima es 180.0")
    private Double longitud;

    /**
     * Fecha y hora para la cual se solicita el pronóstico.
     */
    @NotNull(message = "La fecha y hora del pronóstico es obligatoria")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;

    /**
     * Temperatura estimada en grados Celsius.
     */
    private Double temperatura;

    /**
     * Precipitación acumulada estimada en milímetros (mm).
     */
    @PositiveOrZero(message = "La precipitación estimada no puede ser negativa")
    private Double precipitacionMm;

    /**
     * Código meteorológico WMO (World Meteorological Organization).
     */
    @PositiveOrZero(message = "El código meteorológico WMO no puede ser negativo")
    private Integer codigoClima;

    /**
     * Indicador de severidad climática según RN-07:
     * true si precipitacionMm > 40 o códigos meteorológicos severos (tormenta fuerte / granizo).
     */
    @NotNull(message = "El indicador de condición adversa es obligatorio")
    private Boolean condicionAdversa;

    /**
     * Descripción textual legible de la condición atmosférica prevista.
     */
    private String descripcion;
}
