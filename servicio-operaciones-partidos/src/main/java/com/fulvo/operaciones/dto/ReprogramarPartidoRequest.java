package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Solicitud para reprogramar un partido suspendido con una nueva sede y fecha/hora (FN-08, RN-07).
 */
@Schema(description = "Nueva sede y fecha futura de un partido SUSPENDIDO. Conserva el árbitro asignado y valida disponibilidad horaria")
public record ReprogramarPartidoRequest(
        @NotNull(message = "La nueva sede es obligatoria")
        @Positive(message = "La nueva sede debe ser positiva")
        @Schema(description = "Identificador positivo de una sede existente y disponible",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long nuevaSedeId,

        @NotNull(message = "La nueva fecha y hora son obligatorias")
        @Future(message = "La nueva fecha y hora deben ser futuras")
        @Schema(description = "Nueva fecha y hora local, sin zona horaria. Debe ser futura al enviar la petición; el ejemplo debe ajustarse cuando corresponda",
                example = "2030-12-12T19:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime nuevaFechaHora
) {
}
