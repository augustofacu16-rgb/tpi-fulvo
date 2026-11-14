package com.fulvo.operaciones.dto;

import com.fulvo.operaciones.models.EstadoPartido;

public record EstadoPartidoResponse(
        Long partidoId,
        EstadoPartido estado
) {
}