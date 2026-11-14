package com.fulvo.torneos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Payload para la modificación del estado operativo de un torneo (cierre de inscripción o cancelación)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoTorneoDTO {

    @Schema(description = "Nuevo estado a aplicar: CERRADO_INSCRIPCION (exige 100% cupo cubierto, RN-01) o CANCELADO (flexible, RN-04)", example = "CERRADO_INSCRIPCION", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El estado del torneo es obligatorio")
    private String estado;
}
