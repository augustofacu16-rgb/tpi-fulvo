package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sede registrada con su ubicación geográfica resuelta")
public record SedeResponse(
        @Schema(description = "Identificador generado para la sede", example = "1")
        Long id,
        @Schema(description = "Nombre de la sede", example = "Estadio Kempes")
        String nombre,
        @Schema(description = "Dirección registrada", example = "Estadio Mario Alberto Kempes, Córdoba, Argentina")
        String direccion,
        @Schema(description = "Latitud resuelta por el proveedor o por el respaldo manual",
                example = "-31.4167", minimum = "-90", maximum = "90")
        Double latitud,
        @Schema(description = "Longitud resuelta por el proveedor o por el respaldo manual",
                example = "-64.1833", minimum = "-180", maximum = "180")
        Double longitud
) {
}
