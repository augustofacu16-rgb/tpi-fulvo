package com.fulvo.operaciones.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Solicitud para reprogramar un partido suspendido con una nueva sede y fecha/hora (FN-08, RN-07).
 */
public record ReprogramarPartidoRequest(
        @NotNull(message = "La nueva sede es obligatoria")
        @Positive(message = "La nueva sede debe ser positiva")
        Long nuevaSedeId,

        @NotNull(message = "La nueva fecha y hora son obligatorias")
        @Future(message = "La nueva fecha y hora deben ser futuras")
        LocalDateTime nuevaFechaHora
) {
}
