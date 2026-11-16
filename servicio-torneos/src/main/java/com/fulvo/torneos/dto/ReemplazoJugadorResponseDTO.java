package com.fulvo.torneos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta de un reemplazo de jugador exitoso.
 */
@Schema(description = "Detalle del reemplazo de jugador procesado y estado del cupo de cambios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReemplazoJugadorResponseDTO {

    @Schema(description = "Identificador del equipo", example = "1")
    private Long equipoId;

    @Schema(description = "Nombre oficial del equipo", example = "Los Halcones FC")
    private String equipoNombre;

    @Schema(description = "ID del jugador saliente que recibió baja lógica", example = "14")
    private Long jugadorSalienteId;

    @Schema(description = "DNI del jugador saliente", example = "30111014")
    private String dniSaliente;

    @Schema(description = "ID relacional asignado al nuevo jugador ingresante", example = "16")
    private Long nuevoJugadorId;

    @Schema(description = "DNI del nuevo futbolista incorporado al plantel", example = "30999888")
    private String dniEntrante;

    @Schema(description = "Cantidad acumulada de cambios realizados por el equipo en el torneo", example = "1")
    private Integer cambiosRealizados;

    @Schema(description = "Cambios reglamentarios aún disponibles (máximo 5)", example = "4")
    private Integer cambiosRestantes;
}
