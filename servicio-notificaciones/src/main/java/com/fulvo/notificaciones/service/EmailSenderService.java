package com.fulvo.notificaciones.service;

import com.fulvo.common.dto.NotificacionEmailDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Servicio encargado de la composición y envío de correos electrónicos transaccionales hacia Mailpit.
 * Implementa captura silenciosa de excepciones SMTP para garantizar la resiliencia operativa (RN-12).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:notificaciones@fulvo.com}")
    private String mailFrom;

    @Value("${app.mail.from-name:Fulvo! Notificaciones}")
    private String mailFromName;

    /**
     * Envía un correo electrónico transaccional hacia el servidor SMTP configurado (Mailpit en puerto 1025).
     *
     * @param dto Datos de la notificación por email.
     * @return true si el correo se despachó exitosamente, false si ocurrió una contingencia SMTP.
     */
    public boolean enviarEmail(NotificacionEmailDTO dto) {
        try {
            log.info("Iniciando despacho de email transaccional para evento [{}] a destinatario {}",
                    dto.getTipoEvento(), dto.getDestinatario());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(dto.getDestinatario());
            helper.setSubject(dto.getAsunto());
            helper.setText(dto.getCuerpo(), true); // Contenido renderizable en HTML

            mailSender.send(message);

            log.info("Email [{}] despachado con éxito a Mailpit para destinatario {}",
                    dto.getTipoEvento(), dto.getDestinatario());
            return true;
        } catch (Exception e) {
            // RN-12 y Subsección 4.3: Captura silenciosa de excepciones SMTP
            // para no bloquear ni abortar las transacciones de negocio.
            log.error("Fallo SMTP al enviar correo [{}] a {}: {}. Continuando por resiliencia operativa.",
                    dto.getTipoEvento(), dto.getDestinatario(), e.getMessage(), e);
            return false;
        }
    }
}
