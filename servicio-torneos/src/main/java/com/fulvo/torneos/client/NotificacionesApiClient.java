package com.fulvo.torneos.client;

import com.fulvo.common.dto.NotificacionEmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;

/**
 * Cliente HTTP desacoplado basado en RestClient para invocar el microservicio adaptador
 * servicio-notificaciones (POST /v1/notificaciones/emails) (Subsección 2.4.1).
 * Implementa despacho asíncrono para no degradar el tiempo de respuesta del hilo principal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionesApiClient {

    private final RestClient notificacionesRestClient;

    /** Confirma el envío SMTP informado por el adaptador, no solo la aceptación HTTP. */
    public boolean enviarNotificacionConfirmada(NotificacionEmailDTO notificacion) {
        try {
            ConfirmacionEnvio respuesta = notificacionesRestClient.post()
                    .uri("/v1/notificaciones/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) headers.set("X-Correlation-ID", correlationId);
                    })
                    .body(notificacion)
                    .retrieve()
                    .body(ConfirmacionEnvio.class);
            return respuesta != null && Boolean.TRUE.equals(respuesta.enviado());
        } catch (RestClientException ex) {
            log.warn("No se pudo confirmar la notificación [{}] a {}",
                    notificacion.getTipoEvento(), notificacion.getDestinatario(), ex);
            return false;
        }
    }

    public record ConfirmacionEnvio(Boolean enviado) {}

    /**
     * Envía de forma asíncrona una notificación por email hacia servicio-notificaciones.
     *
     * @param notificacion DTO canónico con el destinatario, asunto, cuerpo y tipo de evento.
     * @return CompletableFuture completado sin retorno o completado tras registrar la contingencia.
     */
    @Async
    public CompletableFuture<Void> enviarNotificacionAsync(NotificacionEmailDTO notificacion) {
        try {
            log.info("Despachando asíncronamente notificación [{}] a {} vía RestClient",
                    notificacion.getTipoEvento(), notificacion.getDestinatario());

            notificacionesRestClient.post()
                    .uri("/v1/notificaciones/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(notificacion)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notificación [{}] despachada exitosamente a servicio-notificaciones para {}",
                    notificacion.getTipoEvento(), notificacion.getDestinatario());
        } catch (Exception e) {
            log.error("Fallo al invocar servicio-notificaciones para evento [{}] hacia {}: {}",
                    notificacion.getTipoEvento(), notificacion.getDestinatario(), e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(null);
    }
}
