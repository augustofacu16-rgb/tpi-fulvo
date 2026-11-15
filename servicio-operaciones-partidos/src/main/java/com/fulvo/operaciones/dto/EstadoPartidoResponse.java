package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fulvo.operaciones.models.EstadoPartido;

@Schema(description = "Resultado de la transición de estado de un partido")
public record EstadoPartidoResponse(
        @Schema(description = "Identificador del partido",
                example = "1")
        Long partidoId,
        @Schema(description = "Estado resultante: EN_CURSO al iniciar o CANCELADO al cancelar por incidentes",
                example = "EN_CURSO")
        EstadoPartido estado
) {
}
