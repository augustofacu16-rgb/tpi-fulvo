package com.fulvo.torneos.dto;

import com.fulvo.torneos.domain.EstadoSolicitudInscripcion;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolverAdmisionDTO {

    @NotNull(message = "El estado de resolución es obligatorio (ACEPTADA o RECHAZADA)")
    private EstadoSolicitudInscripcion estado;

    private String motivoRechazo;
}
