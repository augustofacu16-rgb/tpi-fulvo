package com.fulvo.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la notificación inter-servicio de reprogramación de un partido (RN-07, RN-12).
 */
public record ReprogramacionPartidoDTO(
        @NotNull(message = "El identificador del partido es obligatorio")
        @Positive(message = "El identificador del partido debe ser positivo")
        Long partidoId,

        @NotNull(message = "El identificador del equipo local es obligatorio")
        @Positive(message = "El identificador del equipo local debe ser positivo")
        Long equipoLocalId,

        @NotNull(message = "El identificador del equipo visitante es obligatorio")
        @Positive(message = "El identificador del equipo visitante debe ser positivo")
        Long equipoVisitanteId,

        @NotNull(message = "La nueva fecha y hora del partido es obligatoria")
        LocalDateTime nuevaFechaHora,

        Long nuevaSedeId,

        String motivo
) implements Serializable {
}
