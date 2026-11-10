package com.fulvo.torneos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload de entrada para solicitar la notificación de confirmación de postulación (FN-14).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostulacionNotificacionRequest {

    @NotBlank(message = "El correo destinatario es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    private String emailDestinatario;

    @NotBlank(message = "El nombre del equipo es obligatorio")
    private String nombreEquipo;

    @NotBlank(message = "El nombre del torneo es obligatorio")
    private String nombreTorneo;
}
