package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.AdmisionNotificacionRequest;
import com.fulvo.torneos.dto.PostulacionNotificacionRequest;
import com.fulvo.torneos.service.TorneoNotificacionService;
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
 * Controlador REST para el disparo y prueba de notificaciones de postulación y admisión
 * en servicio-torneos (Subsección 2.4).
 */
@Slf4j
@RestController
@RequestMapping("/v1/torneos/notificaciones")
@RequiredArgsConstructor
public class TorneoNotificacionController {

    private final TorneoNotificacionService torneoNotificacionService;

    /**
     * Dispara asíncronamente la notificación de confirmación de postulación (FN-14).
     */
    @PostMapping("/postulacion")
    public ResponseEntity<Map<String, Object>> notificarPostulacion(@Valid @RequestBody PostulacionNotificacionRequest request) {
        log.info("Solicitud recibida para notificar postulación de equipo '{}' en torneo '{}'",
                request.getNombreEquipo(), request.getNombreTorneo());

        torneoNotificacionService.despacharConfirmacionPostulacion(
                request.getEmailDestinatario(),
                request.getNombreEquipo(),
                request.getNombreTorneo()
        );

        return ResponseEntity.accepted().body(Map.of(
                "mensaje", "Notificación de postulación despachada asíncronamente",
                "destinatario", request.getEmailDestinatario(),
                "nombreEquipo", request.getNombreEquipo(),
                "nombreTorneo", request.getNombreTorneo()
        ));
    }

    /**
     * Dispara asíncronamente la notificación de resolución de admisión (aprobada o rechazada con motivo) (RN-12).
     */
    @PostMapping("/admision")
    public ResponseEntity<Map<String, Object>> notificarAdmision(@Valid @RequestBody AdmisionNotificacionRequest request) {
        log.info("Solicitud recibida para notificar resolución de admisión de equipo '{}' (aprobada={})",
                request.getNombreEquipo(), request.getAprobada());

        torneoNotificacionService.despacharResolucionAdmision(
                request.getEmailDestinatario(),
                request.getNombreEquipo(),
                request.getNombreTorneo(),
                request.getAprobada(),
                request.getMotivoRechazo()
        );

        return ResponseEntity.accepted().body(Map.of(
                "mensaje", "Notificación de resolución de admisión despachada asíncronamente",
                "destinatario", request.getEmailDestinatario(),
                "resolucion", Boolean.TRUE.equals(request.getAprobada()) ? "APROBADA" : "RECHAZADA",
                "nombreEquipo", request.getNombreEquipo(),
                "nombreTorneo", request.getNombreTorneo()
        ));
    }
}
