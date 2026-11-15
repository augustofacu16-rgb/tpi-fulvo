package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fulvo.operaciones.models.EstadoPartido;

import java.time.LocalDateTime;

@Schema(description = "Recursos y horario asignados; la asignación conserva el estado del partido")
public record AsignarPartidoResponse(
        @Schema(description = "Identificador del partido",
                example = "1")
        Long partidoId,
        @Schema(description = "Identificador de la sede asignada",
                example = "1")
        Long sedeId,
        @Schema(description = "DNI del árbitro asignado",
                example = "30111222")
        String arbitroDni,
        @Schema(description = "Fecha y hora local asignadas, sin zona horaria",
                example = "2030-12-10T18:00:00")
        LocalDateTime fechaHora,
        @Schema(description = "Estado del partido después de asignar los recursos",
                example = "PROGRAMADO")
        EstadoPartido estado
) {
}
