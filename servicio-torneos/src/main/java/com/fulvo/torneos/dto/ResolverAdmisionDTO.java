package com.fulvo.torneos.dto;

import com.fulvo.torneos.models.EstadoSolicitudInscripcion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(description = "Payload para la resolución administrativa de admisión o rechazo de una postulación (RN-04)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolverAdmisionDTO {

    @Schema(description = "Resolución emitida: ACEPTADA (admite al equipo y promueve a delegado) o RECHAZADA", example = "ACEPTADA", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El estado de resolución es obligatorio (ACEPTADA o RECHAZADA)")
    private EstadoSolicitudInscripcion estado;

    @Schema(description = "Motivo fundado de rechazo en caso de no ser admitido", example = "No presenta nómina reglamentaria de arqueros")
    private String motivoRechazo;
}
