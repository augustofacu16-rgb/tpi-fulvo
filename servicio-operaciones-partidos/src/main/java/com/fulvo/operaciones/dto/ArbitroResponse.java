package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos del árbitro registrado correctamente")
public record ArbitroResponse (
    @Schema(description = "DNI que identifica al árbitro", example = "30111222")
    String dni,
    @Schema(
            description = "Identificador sub del usuario de Keycloak asociado al árbitro",
            example = "user-arbitro1-uuid"
    )
    String usuarioId,
    @Schema(description = "Nombre del árbitro", example = "Carlos")
    String nombre,
    @Schema(description = "Apellido del árbitro", example = "Baldassi")
    String apellido
) {

}
