package com.fulvo.torneos.dto;

import com.fulvo.torneos.domain.EstadoSolicitudInscripcion;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudInscripcionResponseDTO {

    private Long id;
    private Long torneoId;
    private String fanaticoUsuarioId;
    private String nombreEquipo;
    private EstadoSolicitudInscripcion estado;
    private LocalDateTime fechaSolicitud;
    private String motivoRechazo;
    private Long equipoId;
}
