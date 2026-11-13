package com.fulvo.operaciones.dto;

import com.fulvo.operaciones.models.EstadoPartido;

import java.time.LocalDateTime;

public record AsignarPartidoResponse(
        Long partidoId,
        Long sedeId,
        String arbitroDni,
        LocalDateTime fechaHora,
        EstadoPartido estado
) {
}
