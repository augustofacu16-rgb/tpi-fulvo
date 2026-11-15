package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fulvo.operaciones.models.TipoActuacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Gol o tarjeta de un jugador, identificado por su equipo y dorsal")
public record ActuacionJugadorRequest(
        @Schema(description = "Identificador del equipo local o visitante del partido",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long equipoId,
        @Schema(description = "Dorsal positivo del jugador dentro de su equipo",
                example = "9",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer dorsalJugador,
        @Schema(description = "Tipo de actuación: GOL, TARJETA_AMARILLA o TARJETA_ROJA",
                example = "GOL",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull TipoActuacion tipo
) {
}
