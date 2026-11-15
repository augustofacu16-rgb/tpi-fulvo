package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.BajaEquipoResponseDTO;
import com.fulvo.torneos.dto.ReemplazoJugadorDTO;
import com.fulvo.torneos.dto.ReemplazoJugadorResponseDTO;
import com.fulvo.torneos.service.EquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Equipos", description = "Gestión del plantel, reemplazos de jugadores y bajas/descalificaciones de planteles (Subsección 5.2)")
@RestController
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    @Operation(
            summary = "Reemplazar un jugador del plantel",
            description = "Permite al Delegado sustituir a un futbolista de su lista de buena fe por otro (máximo 5 cambios en competencia, RN-05, RN-11). Prohíbe taxativamente la sustitución de jugadores con suspendido == true. Realiza baja lógica del saliente y alta del ingresante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reemplazo de jugador realizado exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Tope de 5 cambios alcanzado, intento de sustituir a un jugador suspendido, o dorsal/posición reglamentaria inválida (RFC 7807 ProblemDetail)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado: requiere rol ROL_DELEGADO",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Equipo o jugador saliente no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: torneo no iniciado, equipo dado de baja, o DNI entrante ya inscripto en el certamen (RN-02)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/v1/equipos/{id}/reemplazos-jugadores")
    public ResponseEntity<ReemplazoJugadorResponseDTO> reemplazarJugador(
            @Parameter(description = "Identificador único del equipo", example = "1", required = true)
            @PathVariable("id") Long equipoId,
            @Valid @RequestBody ReemplazoJugadorDTO dto) {
        return ResponseEntity.ok(equipoService.reemplazarJugador(equipoId, dto));
    }

    @Operation(
            summary = "Baja voluntaria o descalificación de un equipo",
            description = "Permite a un Delegado solicitar la baja voluntaria de su equipo (tipo=BAJA_VOLUNTARIA) o al Organizador descalificarlo (tipo=DESCALIFICACION) en cualquier momento (RN-04, FN-13). Despacha notificación por email hacia Mailpit (RN-11)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo dado de baja o descalificado exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Tipo de baja inválido (valores permitidos: BAJA_VOLUNTARIA, DESCALIFICACION)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado: requiere rol ROL_ORGANIZADOR o ROL_DELEGADO",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Torneo o equipo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: el equipo ya se encuentra inactivo o el certamen está finalizado/cancelado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/v1/torneos/{idTorneo}/equipos/{idEquipo}")
    public ResponseEntity<BajaEquipoResponseDTO> darDeBajaEquipo(
            @Parameter(description = "Identificador del torneo", example = "1", required = true)
            @PathVariable("idTorneo") Long torneoId,
            @Parameter(description = "Identificador del equipo", example = "1", required = true)
            @PathVariable("idEquipo") Long equipoId,
            @Parameter(description = "Tipo de baja: BAJA_VOLUNTARIA o DESCALIFICACION", example = "BAJA_VOLUNTARIA")
            @RequestParam(value = "tipo", defaultValue = EquipoService.TIPO_BAJA_VOLUNTARIA) String tipo,
            @Parameter(description = "Motivo fundado de la baja o descalificación", example = "Baja solicitada por el delegado")
            @RequestParam(value = "motivo", required = false) String motivo) {
        return ResponseEntity.ok(equipoService.darDeBajaEquipo(torneoId, equipoId, tipo, motivo));
    }
}
