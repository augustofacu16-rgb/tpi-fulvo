package com.fulvo.torneos.service;

import com.fulvo.common.dto.NotificacionEmailDTO;
import com.fulvo.torneos.client.NotificacionesApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de dominio responsable de orquestar y generar los eventos de notificación
 * correspondientes al ciclo de admisiones, gestión de certámenes y contingencias de partidos (FN-14, RN-07, RN-11, RN-12).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TorneoNotificacionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

    /**
     * Despacha la notificación de baja voluntaria o descalificación de un equipo hacia su delegado
     * (RN-04, RN-11, FN-13).
     *
     * @param emailDestinatario Dirección del delegado del equipo.
     * @param nombreEquipo      Nombre del equipo afectado.
     * @param nombreTorneo      Nombre del torneo.
     * @param descalificado     true si el Organizador lo descalificó; false si fue baja voluntaria.
     * @param motivo            Motivo informado (puede ser nulo).
     */
    public void despacharBajaEquipo(String emailDestinatario, String nombreEquipo, String nombreTorneo,
                                    boolean descalificado, String motivo) {
        String asunto = (descalificado ? "Equipo Descalificado - " : "Baja Confirmada - ") + nombreTorneo;
        String titulo = descalificado ? "⛔ Descalificación de Equipo" : "👋 Baja de Equipo Confirmada";
        String color = descalificado ? "#d32f2f" : "#f57c00";
        String detalle = descalificado
                ? "ha sido <strong>DESCALIFICADO</strong> por el Organizador del certamen."
                : "ha sido <strong>dado de baja</strong> del certamen a tu solicitud.";
        String motivoTexto = (motivo != null && !motivo.isBlank()) ? motivo : "Sin motivo informado.";

        String cuerpo = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="background-color: %s; color: white; padding: 15px; border-radius: 5px;">
                        <h2>%s</h2>
                    </div>
                    <p>Estimado/a delegado/a,</p>
                    <p>Te informamos que tu equipo <strong>%s</strong> %s (certamen <strong>%s</strong>).</p>
                    <p><strong>Motivo:</strong> %s</p>
                    <br/>
                    <p>Saludos cordiales,<br/><em>Comité Organizador de Fulvo'</em></p>
                </body>
                </html>
                """.formatted(color, titulo, nombreEquipo, detalle, nombreTorneo, motivoTexto);

        NotificacionEmailDTO dto = NotificacionEmailDTO.builder()
                .destinatario(emailDestinatario)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .tipoEvento(descalificado ? "DESCALIFICACION_EQUIPO" : "BAJA_EQUIPO")
                .fechaHora(LocalDateTime.now())
                .build();

        log.info("Disparando notificación de {} del equipo '{}' en certamen '{}' a {}",
                descalificado ? "descalificación" : "baja voluntaria", nombreEquipo, nombreTorneo, emailDestinatario);
        notificacionesApiClient.enviarNotificacionAsync(dto);
    }

    /**
     * Despacha alerta de suspensión de partido hacia el delegado de un equipo involucrado
     * (RN-07, RN-11, RN-12).
     *
     * @param emailDestinatario Dirección del delegado destinatario.
     * @param nombreEquipo      Nombre del equipo del delegado.
     * @param nombreRival       Nombre del equipo contrincante.
     * @param nombreTorneo      Nombre del torneo.
     * @param partidoId         ID del partido suspendido.
     * @param motivo            Motivo de la suspensión (clima severo, fuerza mayor, etc.).
     * @param fechaOriginal     Fecha y hora original del encuentro.
     */
    public boolean despacharSuspensionPartido(String emailDestinatario, String nombreEquipo, String nombreRival,
                                           String nombreTorneo, Long partidoId, String motivo, LocalDateTime fechaOriginal) {
        String asunto = "Alerta Meteorológica: Partido Suspendido por Tormenta - " + nombreTorneo;
        String fechaTexto = fechaOriginal != null ? fechaOriginal.format(FORMATTER) : "A confirmar";
        String motivoTexto = (motivo != null && !motivo.isBlank())
                ? motivo
                : "Condiciones meteorológicas adversas / tormenta severa.";

        String cuerpo = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="background-color: #d32f2f; color: white; padding: 15px; border-radius: 5px;">
                        <h2>⚠️ Alerta Meteorológica: Partido Suspendido</h2>
                    </div>
                    <p>Estimado/a delegado/a de <strong>%s</strong>,</p>
                    <p>Te comunicamos que el partido <strong>#%d</strong> frente a <strong>%s</strong> correspondiente al certamen <strong>%s</strong> ha sido <span style="color:#d32f2f; font-weight:bold;">SUSPENDIDO</span>.</p>
                    <p><strong>Fecha programada originalmente:</strong> %s</p>
                    <p><strong>Motivo informado:</strong> %s</p>
                    <p>El encuentro queda en estado pendiente y será reprogramado en una nueva fecha y sede por el Comité Organizador.</p>
                    <br/>
                    <p>Saludos cordiales,<br/><em>Comité Organizador de Fulvo'</em></p>
                </body>
                </html>
                """.formatted(nombreEquipo, partidoId, nombreRival, nombreTorneo, fechaTexto, motivoTexto);

        NotificacionEmailDTO dto = NotificacionEmailDTO.builder()
                .destinatario(emailDestinatario)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .tipoEvento("PARTIDO_SUSPENDIDO")
                .fechaHora(LocalDateTime.now())
                .build();

        log.info("Disparando alerta de suspensión de partido #{} para equipo '{}' en certamen '{}' a {}",
                partidoId, nombreEquipo, nombreTorneo, emailDestinatario);
        return notificacionesApiClient.enviarNotificacionConfirmada(dto);
    }

    /**
     * Despacha notificación de reprogramación de partido hacia el delegado de un equipo involucrado.
     *
     * @param emailDestinatario Dirección del delegado destinatario.
     * @param nombreEquipo      Nombre del equipo del delegado.
     * @param nombreRival       Nombre del equipo contrincante.
     * @param nombreTorneo      Nombre del torneo.
     * @param partidoId         ID del partido reprogramado.
     * @param motivo            Motivo informado.
     * @param nuevaFechaHora    Nueva fecha y hora fijada para la disputa.
     * @param nuevaSedeId       Nueva sede asignada.
     */
    public boolean despacharReprogramacionPartido(String emailDestinatario, String nombreEquipo, String nombreRival,
                                              String nombreTorneo, Long partidoId, String motivo,
                                              LocalDateTime nuevaFechaHora, Long nuevaSedeId) {
        String asunto = "Partido Reprogramado - " + nombreTorneo;
        String fechaTexto = nuevaFechaHora != null ? nuevaFechaHora.format(FORMATTER) : "A confirmar";
        String sedeTexto = nuevaSedeId != null ? "Sede #" + nuevaSedeId : "Sede a confirmar";
        String motivoTexto = (motivo != null && !motivo.isBlank()) ? motivo : "Reasignación logística de fecha y predio.";

        String cuerpo = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="background-color: #1976d2; color: white; padding: 15px; border-radius: 5px;">
                        <h2>📅 Notificación de Partido Reprogramado</h2>
                    </div>
                    <p>Estimado/a delegado/a de <strong>%s</strong>,</p>
                    <p>Te informamos que el partido <strong>#%d</strong> frente a <strong>%s</strong> (certamen <strong>%s</strong>) ha sido <span style="color:#1976d2; font-weight:bold;">REPROGRAMADO</span>.</p>
                    <p><strong>Nueva Fecha y Hora de Disputa:</strong> %s</p>
                    <p><strong>Lugar:</strong> %s</p>
                    <p><strong>Detalle:</strong> %s</p>
                    <br/>
                    <p>Por favor comunicarle a tu plantel la actualización de la jornada.</p>
                    <br/>
                    <p>Saludos cordiales,<br/><em>Comité Organizador de Fulvo'</em></p>
                </body>
                </html>
                """.formatted(nombreEquipo, partidoId, nombreRival, nombreTorneo, fechaTexto, sedeTexto, motivoTexto);

        NotificacionEmailDTO dto = NotificacionEmailDTO.builder()
                .destinatario(emailDestinatario)
                .asunto(asunto)
                .cuerpo(cuerpo)
                .tipoEvento("PARTIDO_REPROGRAMADO")
                .fechaHora(LocalDateTime.now())
                .build();

        log.info("Disparando notificación de reprogramación de partido #{} para equipo '{}' en certamen '{}' a {}",
                partidoId, nombreEquipo, nombreTorneo, emailDestinatario);
        return notificacionesApiClient.enviarNotificacionConfirmada(dto);
    }

    /**
     * Resuelve la dirección de correo de un usuario a partir de su identificador.
     * Mientras no exista integración con Keycloak para consultar el email del usuario, si el identificador
     * ya es un correo se usa tal cual; en caso contrario se emplea la casilla de pruebas de Mailpit.
     */
    public String resolverEmailDestinatario(String usuarioId) {
        if (usuarioId != null && usuarioId.contains("@")) {
            return usuarioId;
        }
        return "fanatico1@fulvo.com";
    }
}
