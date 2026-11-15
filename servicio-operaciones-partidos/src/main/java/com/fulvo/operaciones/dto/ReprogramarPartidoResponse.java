package com.fulvo.operaciones.dto;

import com.fulvo.operaciones.models.EstadoPartido;

import java.time.LocalDateTime;

/**
 * Respuesta tras reprogramar un partido suspendido al estado REPROGRAMADO.
 */
public record ReprogramarPartidoResponse(
        Long partidoId,
        Long sedeId,
        String nombreSede,
        LocalDateTime fechaHora,
        EstadoPartido estado
) {
}
