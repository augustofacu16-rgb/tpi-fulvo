package com.fulvo.operaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Solicitud para reprogramar un partido suspendido con una nueva sede y fecha/hora (FN-08, RN-07).
 */
public record ReprogramarPartidoRequest(
        @NotNull(message = "La nueva sede es obligatoria")
        Long nuevaSedeId,

        @NotNull(message = "La nueva fecha y hora son obligatorias")
        LocalDateTime nuevaFechaHora
) {
}
