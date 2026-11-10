package com.fulvo.notificaciones.controller;

import com.fulvo.common.dto.NotificacionEmailDTO;
import com.fulvo.notificaciones.service.EmailSenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para el adaptador de notificaciones por email.
 * Expone el endpoint canónico POST /v1/notificaciones/emails (Subsecciones 2.4 y 4.3).
 */
@Slf4j
@RestController
@RequestMapping("/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionEmailController {

    private final EmailSenderService emailSenderService;

    @PostMapping("/emails")
    public ResponseEntity<Map<String, Object>> enviarEmail(@Valid @RequestBody NotificacionEmailDTO notificacion) {
        log.info("Petición recibida en POST /v1/notificaciones/emails: evento={}, destinatario={}",
                notificacion.getTipoEvento(), notificacion.getDestinatario());

        boolean enviado = emailSenderService.enviarEmail(notificacion);

        return ResponseEntity.accepted().body(Map.of(
                "mensaje", "Notificación recibida y procesada",
                "tipoEvento", notificacion.getTipoEvento(),
                "destinatario", notificacion.getDestinatario(),
                "enviado", enviado
        ));
    }
}
