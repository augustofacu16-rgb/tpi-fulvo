package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fulvo.operaciones.models.EstadoPartido;

@Schema(description = "Resultado del partido finalizado tras registrar el acta",
        example = "{\"partidoId\":1,\"estado\":\"FINALIZADO\",\"golesLocal\":1,\"golesVisitante\":0,\"penalesLocal\":null,\"penalesVisitante\":null,\"ganadorEquipoId\":1}")
public record ActaResponse(
        @Schema(description = "Identificador del partido finalizado",
                example = "1")
        Long partidoId,
        @Schema(description = "Estado resultante tras registrar el acta",
                example = "FINALIZADO")
        EstadoPartido estado,
        @Schema(description = "Goles registrados para el local",
                example = "1")
        Integer golesLocal,
        @Schema(description = "Goles registrados para el visitante",
                example = "0")
        Integer golesVisitante,
        @Schema(description = "Penales del local; nulo cuando no hubo definición por penales",
                example = "4",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        Integer penalesLocal,
        @Schema(description = "Penales del visitante; nulo cuando no hubo definición por penales",
                example = "3",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        Integer penalesVisitante,
        @Schema(description = "Equipo ganador, incluyendo la definición por penales si corresponde. Nulo en un empate de Liga",
                example = "1",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        Long ganadorEquipoId
) {
}
