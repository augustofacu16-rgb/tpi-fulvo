package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.AdmisionNotificacionRequest;
import com.fulvo.torneos.dto.PostulacionNotificacionRequest;
import com.fulvo.torneos.service.TorneoNotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para el disparo y prueba de notificaciones de postulación y admisión
 * en servicio-torneos (Subsección 2.4 y 5.2).
 */
@Slf4j
@RestController
@RequestMapping("/v1/torneos/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones de Torneo", description = "Endpoints de disparo y prueba de alertas por correo electrónico hacia Mailpit (Subsección 5.2)")
public class TorneoNotificacionController {

    private final TorneoNotificacionService torneoNotificacionService;

    @Operation(
            summary = "Notificar confirmación de postulación",
            description = "Dispara asíncronamente un correo HTML hacia la casilla del fanático postulante confirmando la recepción de su lista de buena fe (FN-14)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Notificación de postulación despachada asíncronamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido o correo malformado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
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

    @Operation(
            summary = "Notificar resolución de admisión (aprobada o rechazada)",
            description = "Dispara asíncronamente un correo HTML hacia el delegado con la resolución de admisión o el motivo expreso de rechazo (RN-12)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Notificación de resolución de admisión despachada asíncronamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido o correo malformado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
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
