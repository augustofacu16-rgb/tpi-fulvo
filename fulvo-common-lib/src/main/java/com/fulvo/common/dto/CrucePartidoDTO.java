package com.fulvo.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Long torneoId;

    /**
     * Formato de competencia: "LIGA" o "LLAVES".
     */
    private String formatoTorneo;

    /**
     * Número de fecha (Liga) o identificador de fase eliminatoria (Llaves: p.ej. 1=8vos, 2=4tos, 3=Semis, 4=Final).
     */
    private Integer fechaOFase;

    /**
     * Identificador lógico del equipo que ejerce la condición de local.
     * Puede ser null en fases avanzadas de Llaves antes de determinarse el clasificado.
     */
    private Long equipoLocalId;

    /**
     * Identificador lógico del equipo que ejerce la condición de visitante.
     * Puede ser null en fases avanzadas de Llaves antes de determinarse el clasificado.
     */
    private Long equipoVisitanteId;

    /**
     * Fecha y hora fijada para el encuentro (opcional inicialmente, configurable en asignación horaria).
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
    private String estado = "PROGRAMADO";
}
