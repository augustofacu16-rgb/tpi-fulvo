package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.CrearSolicitudInscripcionDTO;
import com.fulvo.torneos.dto.ResolverAdmisionDTO;
import com.fulvo.torneos.dto.SolicitudInscripcionResponseDTO;
import com.fulvo.torneos.service.SolicitudInscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Solicitudes de Inscripción", description = "Endpoints para la postulación de equipos con lista de buena fe y resolución administrativa de admisiones")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SolicitudInscripcionController {

    private final SolicitudInscripcionService solicitudService;

    @Operation(
            summary = "Postular equipo con lista de buena fe (15 a 22 jugadores)",
            description = "Permite a un usuario con rol Fanático enviar una única solicitud de postulación por certamen (RN-03). Exige entre 15 y 22 futbolistas, al menos 1 arquero, exactamente 1 capitán y DNI único por torneo (RN-02). Devuelve HTTP 202 Accepted y despacha confirmación por email (FN-14)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Solicitud de postulación aceptada para evaluación"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nómina inválida (fuera del rango 15-22, sin arquero o sin capitán único) (RFC 7807 ProblemDetail)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado: requiere rol ROL_FANATICO",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Torneo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: doble inscripción del fanático (RN-03), DNI duplicado en otro equipo del torneo (RN-02), o torneo no abierto",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/v1/torneos/{id}/solicitudes-inscripcion")
    public ResponseEntity<SolicitudInscripcionResponseDTO> postularEquipo(
            @Parameter(description = "Identificador único del certamen", example = "1", required = true)
            @PathVariable("id") Long torneoId,
            @Parameter(description = "Identificador o UUID del usuario solicitante provisto por el Gateway / Keycloak", example = "uuid-keycloak-fanatico")
            @RequestHeader(value = "X-User-Id", defaultValue = "fanatico-default-id") String fanaticoUsuarioId,
            @Valid @RequestBody CrearSolicitudInscripcionDTO dto) {

        log.info("Petición recibida para postular equipo '{}' en torneo #{} por usuario {}", dto.getNombreEquipo(), torneoId, fanaticoUsuarioId);
        SolicitudInscripcionResponseDTO response = solicitudService.postularEquipo(torneoId, fanaticoUsuarioId, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(
            summary = "Resolver admisión o rechazo de postulación",
            description = "Permite al Organizador aprobar o rechazar formalmente una solicitud de inscripción (FN-05, RN-04). Si se aprueba, crea la entidad Equipo en esquema_torneos, reduce el cupo y promueve al usuario a Delegado; si se rechaza, asienta el motivo. Despacha notificación por email a Mailpit (RN-12)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resolución de admisión procesada exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado de resolución inválido o datos malformados (RFC 7807 ProblemDetail)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado: requiere rol ROL_ORGANIZADOR",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Solicitud de inscripción no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: cupo del torneo agotado, solicitud ya resuelta, o torneo no abierto",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PatchMapping("/v1/solicitudes-inscripcion/{id}/admision")
    public ResponseEntity<SolicitudInscripcionResponseDTO> resolverAdmision(
            @Parameter(description = "Identificador único de la solicitud de inscripción", example = "1", required = true)
            @PathVariable("id") Long solicitudId,
            @Valid @RequestBody ResolverAdmisionDTO dto) {

        log.info("Petición recibida para resolver admisión de solicitud #{} (estado: {})", solicitudId, dto.getEstado());
        SolicitudInscripcionResponseDTO response = solicitudService.resolverAdmision(solicitudId, dto);
        return ResponseEntity.ok(response);
    }
}
