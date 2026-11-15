package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Resultado y actuaciones para finalizar un partido EN_CURSO. El usuario debe ser el árbitro asignado",
        example = "{\"golesLocal\":1,\"golesVisitante\":0,\"actuaciones\":[{\"equipoId\":1,\"dorsalJugador\":9,\"tipo\":\"GOL\"}]}")
public record RegistrarActaRequest(
        @Schema(description = "Goles del equipo local. Deben coincidir con sus actuaciones de tipo GOL",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Min(0) Integer golesLocal,
        @Schema(description = "Goles del equipo visitante. Deben coincidir con sus actuaciones de tipo GOL",
                example = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Min(0) Integer golesVisitante,
        @Schema(description = "Penales convertidos por el local. Solo corresponden a un empate en LLAVES; deben enviarse junto con penalesVisitante y determinar un ganador",
                example = "4",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        @Min(0) Integer penalesLocal,
        @Schema(description = "Penales convertidos por el visitante. Solo corresponden a un empate en LLAVES; deben enviarse junto con penalesLocal y determinar un ganador",
                example = "3",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        @Min(0) Integer penalesVisitante,
        @Schema(description = "Actuaciones de jugadores de los equipos del partido. La lista es obligatoria y puede estar vacía si no hubo goles ni tarjetas",
                example = "[{\"equipoId\":1,\"dorsalJugador\":9,\"tipo\":\"GOL\"}]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull List<@Valid ActuacionJugadorRequest> actuaciones
) {
}
