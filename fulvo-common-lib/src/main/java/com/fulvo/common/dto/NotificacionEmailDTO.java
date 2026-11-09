package com.fulvo.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para el envío de notificaciones por correo electrónico transaccionales
 * hacia servicio-notificaciones (POST /v1/notificaciones/emails) cubriendo los 7 eventos del ciclo (RN-11).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEmailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Dirección de correo electrónico del destinatario.
     */
    @NotBlank(message = "El correo destinatario es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    private String destinatario;

    /**
     * Asunto del correo electrónico.
     */
    @NotBlank(message = "El asunto del correo no puede estar vacío")
    @Size(max = 200, message = "El asunto no puede superar los 200 caracteres")
    private String asunto;

    /**
     * Cuerpo del mensaje o contenido HTML/texto del correo.
     */
    @NotBlank(message = "El contenido del correo no puede estar vacío")
    private String cuerpo;

    /**
     * Tipo de evento notificado:
     * SOLICITUD_ENVIADA, SOLICITUD_ACEPTADA, SOLICITUD_RECHAZADA,
     * BAJA_EQUIPO, DESCALIFICACION_EQUIPO, CANCELACION_TORNEO, PARTIDO_SUSPENDIDO.
     */
    @NotBlank(message = "El tipo de evento notificado es obligatorio")
    @Pattern(
        regexp = "^(SOLICITUD_ENVIADA|SOLICITUD_ACEPTADA|SOLICITUD_RECHAZADA|BAJA_EQUIPO|DESCALIFICACION_EQUIPO|CANCELACION_TORNEO|PARTIDO_SUSPENDIDO)$",
        message = "Tipo de evento de notificación no reconocido"
    )
    private String tipoEvento;

    /**
     * Marca temporal del evento notificado.
     */
    @NotNull(message = "La marca temporal es obligatoria")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
}
