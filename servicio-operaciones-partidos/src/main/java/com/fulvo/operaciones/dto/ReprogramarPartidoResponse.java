package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fulvo.operaciones.models.EstadoPartido;

import java.time.LocalDateTime;

/**
 * Respuesta tras reprogramar un partido suspendido al estado REPROGRAMADO.
 */
@Schema(description = "Sede, fecha y estado del partido reprogramado y sincronizado con Torneos")
public record ReprogramarPartidoResponse(
        @Schema(description = "Identificador del partido reprogramado",
                example = "1")
        Long partidoId,
        @Schema(description = "Identificador de la nueva sede",
                example = "2")
        Long sedeId,
        @Schema(description = "Nombre de la nueva sede",
                example = "Predio Municipal")
        String nombreSede,
        @Schema(description = "Nueva fecha y hora local del encuentro, sin zona horaria",
                example = "2030-12-12T19:00:00")
        LocalDateTime fechaHora,
        @Schema(description = "Estado resultante de la reprogramación",
                example = "REPROGRAMADO")
        EstadoPartido estado
) {
}
