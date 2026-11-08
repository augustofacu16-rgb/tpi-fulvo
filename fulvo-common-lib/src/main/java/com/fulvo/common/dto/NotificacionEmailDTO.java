package com.fulvo.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String destinatario;

    /**
     * Asunto del correo electrónico.
     */
    private String asunto;

    /**
     * Cuerpo del mensaje o contenido HTML/texto del correo.
     */
    private String cuerpo;

    /**
     * Tipo de evento notificado:
     * SOLICITUD_ENVIADA, SOLICITUD_ACEPTADA, SOLICITUD_RECHAZADA,
     * BAJA_EQUIPO, DESCALIFICACION_EQUIPO, CANCELACION_TORNEO, PARTIDO_SUSPENDIDO.
     */
    private String tipoEvento;

    /**
     * Marca temporal del evento notificado.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
}
