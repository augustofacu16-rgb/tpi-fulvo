package com.fulvo.notificaciones.controller;

import com.fulvo.common.dto.NotificacionEmailDTO;
import com.fulvo.notificaciones.service.EmailSenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
 * Controlador REST para el adaptador de notificaciones por email.
 * Expone el endpoint canónico POST /v1/notificaciones/emails (Subsecciones 2.4 y 4.3).
 */
@Slf4j
@RestController
@RequestMapping("/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Adaptador de despacho de correos transaccionales vía SMTP (Mailpit)")
public class NotificacionEmailController {

    private final EmailSenderService emailSenderService;

    @Operation(
            summary = "Enviar correo electrónico transaccional",
            description = "Recibe una notificación validada y la despacha vía JavaMailSender a Mailpit. " +
                    "Ante fallos SMTP captura la excepción silenciosamente para no abortar la transacción del llamador (RN-12)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Notificación aceptada y procesada"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de notificación inválidos (RFC 7807 ProblemDetail)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/emails")
    public ResponseEntity<Map<String, Object>> enviarEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del correo transaccional a despachar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificacionEmailDTO.class),
                            examples = @ExampleObject(
                                    name = "Solicitud Enviada",
                                    value = """
                                            {
                                              "destinatario": "delegado@fulvo.com",
                                              "asunto": "Postulacion recibida - Torneo Apertura",
                                              "cuerpo": "<h1>Postulacion Recibida</h1><p>Tu equipo fue postulado correctamente.</p>",
                                              "tipoEvento": "SOLICITUD_ENVIADA",
                                              "fechaHora": "2026-11-13T20:45:00"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody NotificacionEmailDTO notificacion) {
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
