package com.fulvo.torneos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuerpo de POST /v1/equipos/{id}/reemplazos-jugadores (FN-11).
 * Identifica al jugador saliente por su ID autogenerado (Long PK) conforme a DISENO_SISTEMA_FULVO.md.
 */
@Schema(description = "Solicitud de reemplazo de un jugador en la lista de buena fe de un equipo en competencia (máximo 5 cambios)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReemplazoJugadorDTO {

    @Schema(description = "ID del jugador saliente a dar de baja lógica (no puede estar suspendido, RN-11)", example = "14", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del jugador saliente es obligatorio")
    private Long jugadorSalienteId;

    @Schema(description = "Datos del nuevo futbolista entrante", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Los datos del nuevo jugador son obligatorios")
    @Valid
    private JugadorInscripcionDTO nuevoJugador;
}
