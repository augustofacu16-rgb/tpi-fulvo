package com.fulvo.torneos.service;

import com.fulvo.common.dto.NotificacionEmailDTO;
import com.fulvo.torneos.client.NotificacionesApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de dominio responsable de orquestar y generar los eventos de notificación
 * correspondientes al ciclo de admisiones y gestión de certámenes (FN-14, RN-12).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TorneoNotificacionService {

    private final NotificacionesApiClient notificacionesApiClient;

    /**
     * Despacha confirmación al fanático ante postulación de equipo (FN-14).
     *
     * @param emailDestinatario Dirección del fanático postulante.
     * @param nombreEquipo      Nombre del equipo inscripto.
     * @param nombreTorneo      Nombre del torneo.
     */
    public void despacharConfirmacionPostulacion(String emailDestinatario, String nombreEquipo, String nombreTorneo) {
        String asunto = "Postulación Recibida - " + nombreTorneo;
        String cuerpo = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="background-color: #2e7d32; color: white; padding: 15px; border-radius: 5px;">
                        <h2>⚽ ¡Postulación Recibida en Fulvo'!</h2>
                    </div>
                    <p>Hola,</p>
                    <p>Hemos recibido correctamente la solicitud de inscripción para tu equipo <strong>%s</strong> en el certamen <strong>%s</strong>.</p>
                    <p>Tu lista de buena fe está siendo evaluada por la organización del torneo conforme a las reglas del campeonato.</p>
                    <p>Te notificaremos a la brevedad una vez emitida la resolución de admisión.</p>
                    <br/>
                    <p>Saludos cordiales,<br/><em>Comité Organizador de Fulvo'</em></p>
                </body>
                </html>
                """.formatted(nombreEquipo, nombreTorneo);

        NotificacionEmailDTO dto = NotificacionEmailDTO.builder()
                .destinatario(emailDestinatario)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .tipoEvento("SOLICITUD_ENVIADA")
                .fechaHora(LocalDateTime.now())
                .build();

        log.info("Disparando confirmación de postulación para equipo '{}' en certamen '{}' a {}",
                nombreEquipo, nombreTorneo, emailDestinatario);
        notificacionesApiClient.enviarNotificacionAsync(dto);
    }

    /**
     * Despacha notificación de resolución de admisión (aprobado o rechazado con motivo)
     * hacia el delegado vía Mailpit (RN-12, FN-05, RN-04).
     *
     * @param emailDestinatario Dirección del delegado/solicitante.
     * @param nombreEquipo      Nombre del equipo.
     * @param nombreTorneo      Nombre del torneo.
     * @param aprobada          true si fue aprobada; false si fue rechazada.
     * @param motivoRechazo     Motivo de rechazo en caso de no ser admitido.
     */
    public void despacharResolucionAdmision(String emailDestinatario, String nombreEquipo, String nombreTorneo, boolean aprobada, String motivoRechazo) {
        if (aprobada) {
            String asunto = "Admisión Aprobada - Bienvenido a Fulvo'";
            String cuerpo = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <div style="background-color: #1976d2; color: white; padding: 15px; border-radius: 5px;">
                            <h2>🎉 ¡Admisión Aprobada! Bienvenido a Fulvo'</h2>
                        </div>
                        <p>Estimado/a delegado/a,</p>
                        <p>Nos complace comunicarte que la solicitud de inscripción para el equipo <strong>%s</strong> en el certamen <strong>%s</strong> ha sido <span style="color:#2e7d32; font-weight:bold;">APROBADA</span>.</p>
                        <p>A partir de este momento adquieres formalmente el rol de <strong>Delegado de equipo</strong> para este certamen.</p>
                        <p>Podrás gestionar la lista de buena fe y coordinar la participación de tu equipo antes del inicio del fixture.</p>
                        <br/>
                        <p>¡Muchos éxitos en la competencia!<br/><em>Comité Organizador de Fulvo'</em></p>
                    </body>
                    </html>
                    """.formatted(nombreEquipo, nombreTorneo);

            NotificacionEmailDTO dto = NotificacionEmailDTO.builder()
                    .destinatario(emailDestinatario)
                    .asunto(asunto)
                    .cuerpo(cuerpo)
                    .tipoEvento("SOLICITUD_ACEPTADA")
                    .fechaHora(LocalDateTime.now())
                    .build();

            log.info("Disparando aprobación de admisión para equipo '{}' en certamen '{}' a {}",
                    nombreEquipo, nombreTorneo, emailDestinatario);
            notificacionesApiClient.enviarNotificacionAsync(dto);
        } else {
            String asunto = "Admisión Rechazada - " + nombreTorneo;
            String motivo = (motivoRechazo != null && !motivoRechazo.isBlank())
                    ? motivoRechazo
                    : "No cumple con las pautas reglamentarias de la competencia.";

            String cuerpo = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <div style="background-color: #d32f2f; color: white; padding: 15px; border-radius: 5px;">
                            <h2>❌ Resolución de Admisión de Equipo</h2>
                        </div>
                        <p>Estimado/a postulante,</p>
                        <p>Te informamos que la solicitud de inscripción para el equipo <strong>%s</strong> en el certamen <strong>%s</strong> ha sido <span style="color:#d32f2f; font-weight:bold;">RECHAZADA</span>.</p>
                        <p><strong>Motivo de la resolución:</strong> %s</p>
                        <br/>
                        <p>Agradecemos tu interés en participar.<br/><em>Comité Organizador de Fulvo'</em></p>
                    </body>
                    </html>
                    """.formatted(nombreEquipo, nombreTorneo, motivo);

            NotificacionEmailDTO dto = NotificacionEmailDTO.builder()
                    .destinatario(emailDestinatario)
                    .asunto(asunto)
                    .cuerpo(cuerpo)
                    .tipoEvento("SOLICITUD_RECHAZADA")
                    .fechaHora(LocalDateTime.now())
                    .build();

            log.info("Disparando rechazo de admisión para equipo '{}' en certamen '{}' con motivo '{}' a {}",
                    nombreEquipo, nombreTorneo, motivo, emailDestinatario);
            notificacionesApiClient.enviarNotificacionAsync(dto);
        }
    }
}
