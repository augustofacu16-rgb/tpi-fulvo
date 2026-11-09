package com.fulvo.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la comunicación inter-servicio y despacho en lote de partidos generados
 * desde servicio-torneos hacia servicio-operaciones-partidos (POST /v1/partidos/lote).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrucePartidoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador lógico del torneo.
     */
    @NotNull(message = "El identificador del torneo es obligatorio")
    private Long torneoId;

    /**
     * Formato de competencia: "LIGA" o "LLAVES".
     */
    @NotBlank(message = "El formato de torneo es obligatorio")
    @Pattern(regexp = "^(LIGA|LLAVES)$", message = "El formato de torneo debe ser LIGA o LLAVES")
    private String formatoTorneo;

    /**
     * Número de fecha (Liga) o identificador de fase eliminatoria (Llaves).
     */
    @NotNull(message = "La fecha o fase es obligatoria")
    private Integer fechaOFase;

    /**
     * Identificador lógico del equipo que ejerce la condición de local.
     * Puede ser null en fases avanzadas de Llaves antes de determinarse el clasificado.
     */
    @Positive(message = "El identificador del equipo local debe ser positivo")
    private Long equipoLocalId;

    /**
     * Identificador lógico del equipo que ejerce la condición de visitante.
     * Puede ser null en fases avanzadas de Llaves antes de determinarse el clasificado.
     */
    @Positive(message = "El identificador del equipo visitante debe ser positivo")
    private Long equipoVisitanteId;

    /**
     * Fecha y hora fijada para el encuentro (opcional en generación de fixture, configurable en asignación horaria).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;

    /**
     * Identificador de la sede designada (opcional en generación de fixture).
     */
    private Long sedeId;

    /**
     * DNI del árbitro asignado para dirigir el encuentro (opcional en generación de fixture).
     */
    private String arbitroDni;

    /**
     * Estado inicial del encuentro. Por defecto: "PROGRAMADO".
     */
    @Builder.Default
    @Pattern(regexp = "^(PROGRAMADO|EN_CURSO|FINALIZADO|REPROGRAMADO|SUSPENDIDO|CANCELADO)$", message = "Estado de partido inválido")
    private String estado = "PROGRAMADO";
}
