package com.fulvo.operaciones.client;

import com.fulvo.common.dto.NotificacionEmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP basado en RestClient para despachar notificaciones por email
 * hacia servicio-notificaciones (POST /v1/notificaciones/emails) (RN-12).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionesApiClient {

    private final RestClient notificacionesRestClient;

    /**
     * Envía una notificación por correo electrónico sin propagar errores de red/SMTP
     * para preservar la transacción operativa del llamador (RN-12).
     *
     * @param notificacion DTO con destinatario, asunto, cuerpo y tipo de evento.
     */
    public void enviarNotificacion(NotificacionEmailDTO notificacion) {
        String correlationId = MDC.get("correlationId");
        try {
            log.info("Despachando notificación [{}] a {} vía servicio-notificaciones",
                    notificacion.getTipoEvento(), notificacion.getDestinatario());

            notificacionesRestClient.post()
                    .uri("/v1/notificaciones/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (correlationId != null) {
                            headers.set("X-Correlation-ID", correlationId);
                        }
                    })
                    .body(notificacion)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notificación [{}] despachada exitosamente a {}",
                    notificacion.getTipoEvento(), notificacion.getDestinatario());
        } catch (Exception ex) {
            // RN-12: Un fallo en el adaptador de notificaciones no debe revertir la suspensión del partido
            log.error("Fallo al invocar servicio-notificaciones para evento [{}] hacia {}: {}",
                    notificacion.getTipoEvento(), notificacion.getDestinatario(), ex.getMessage(), ex);
        }
    }
}
