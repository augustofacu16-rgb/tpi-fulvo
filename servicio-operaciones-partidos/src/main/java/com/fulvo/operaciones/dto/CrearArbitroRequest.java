package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos obligatorios para registrar un árbitro identificado por su DNI")
public class CrearArbitroRequest {

    @Schema(
            description = "DNI del árbitro. Identificador único; no puede repetirse entre árbitros",
            example = "30111222",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(max = 255)
    private String dni;

    @Schema(
            description = "Identificador sub del usuario de Keycloak asociado al árbitro. "
                    + "No es el nombre de usuario. El alta no verifica su existencia en Keycloak",
            example = "user-arbitro1-uuid",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(max = 255)
    private String usuarioId;

    @Schema(
            description = "Nombre del árbitro",
            example = "Carlos",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(max = 255)
    private String nombre;

    @Schema(
            description = "Apellido del árbitro",
            example = "Baldassi",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(max = 255)
    private String apellido;

}
