package com.fulvo.torneos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fulvo.torneos.models.EstadoSolicitudInscripcion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Respuesta del estado y resolución de una solicitud de inscripción de equipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudInscripcionResponseDTO {

    @Schema(description = "Identificador único de la solicitud", example = "1")
    private Long id;

    @Schema(description = "Identificador del torneo al que postula", example = "1")
    private Long torneoId;

    @Schema(description = "UUID o identificador del usuario fanático postulante", example = "uuid-keycloak-fanatico")
    private String fanaticoUsuarioId;

    @Schema(description = "Nombre oficial del equipo", example = "Los Halcones FC")
    private String nombreEquipo;

    @Schema(description = "Estado de la solicitud: PENDIENTE, ACEPTADA, RECHAZADA", example = "ACEPTADA")
    private EstadoSolicitudInscripcion estado;

    @Schema(description = "Marca temporal de presentación de la solicitud", example = "2026-03-20T14:15:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaSolicitud;

    @Schema(description = "Motivo expreso de rechazo en caso de no ser admitido", example = "Nómina incompleta")
    private String motivoRechazo;

    @Schema(description = "Identificador del equipo creado en el certamen (si fue aceptada)", example = "5")
    private Long equipoId;
}
