package com.fulvo.torneos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload de entrada para solicitar la notificación de confirmación de postulación (FN-14).
 */
@Schema(description = "Solicitud para el disparo de notificación por correo de postulación de equipo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostulacionNotificacionRequest {

    @Schema(description = "Correo electrónico del postulante destinatario", example = "fanatico1@fulvo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El correo destinatario es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    private String emailDestinatario;

    @Schema(description = "Nombre oficial del equipo inscripto", example = "Los Halcones FC", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del equipo es obligatorio")
    private String nombreEquipo;

    @Schema(description = "Nombre del torneo al que postula", example = "Torneo Apertura 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del torneo es obligatorio")
    private String nombreTorneo;
}
