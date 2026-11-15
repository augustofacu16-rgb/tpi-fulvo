package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos de alta de una sede. Las coordenadas manuales son un respaldo opcional y deben indicarse juntas")
public class CrearSedeRequest {

    @Schema(description = "Nombre de la sede", example = "Estadio Kempes",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 255)
    private String nombre;

    @NotBlank
    @Schema(description = "Dirección que se enviará al servicio de geocodificación",
            example = "Estadio Mario Alberto Kempes, Córdoba, Argentina",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 5, max = 250)
    private String direccion;

    @Schema(description = "Latitud de respaldo. Requiere longitudManual; solo se usa si falla la resolución externa",
            example = "-31.4167", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitudManual;

    @Schema(description = "Longitud de respaldo. Requiere latitudManual; solo se usa si falla la resolución externa",
            example = "-64.1833", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitudManual;

    @AssertTrue(message = "Debe indicar ambas coordenadas manuales o ninguna")
    @Schema(hidden = true)
    public boolean isCoordenadasManualesCompletas() {
        return (latitudManual == null) == (longitudManual == null);
    }
}
