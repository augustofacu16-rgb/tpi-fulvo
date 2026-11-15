package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Sede, árbitro y horario para asignar o reemplazar los recursos de un partido")
public record AsignarPartidoRequest(
        @NotNull
        @Schema(description = "Identificador de una sede existente",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long sedeId,

        @NotBlank
        @Schema(description = "DNI de un árbitro existente",
                example = "30111222",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String arbitroDni,

        @NotNull
        @Schema(description = "Fecha y hora local del encuentro, sin zona horaria. Se exige una separación mínima de dos horas respecto de otros partidos de la sede y del árbitro",
                example = "2030-12-10T18:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime fechaHora
) {
}
