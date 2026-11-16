package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.CronogramaTorneoResponseDTO;
import com.fulvo.torneos.dto.PartidoPendienteDTO;
import com.fulvo.torneos.service.CronogramaTorneoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de cronograma general y seguimiento de partidos pendientes
 * para delegados y fanáticos.
 */
@Slf4j
@RestController
@RequestMapping("/v1/torneos/{torneoId}")
@RequiredArgsConstructor
@Tag(name = "Cronograma y Partidos Pendientes", description = "Endpoints de consulta de estado del cronograma, suspensiones y fechas pospuestas")
public class CronogramaController {

    private final CronogramaTorneoService cronogramaTorneoService;

    @Operation(
            summary = "Consultar cronograma y contingencias del certamen",
            description = "Retorna el estado general del torneo con el balance de partidos afectados por suspensión o reprogramación sin corromper cruces."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cronograma obtenido exitosamente"),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Torneo no encontrado (RFC 7807 ProblemDetail)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/cronograma")
    public ResponseEntity<CronogramaTorneoResponseDTO> obtenerCronograma(
            @Parameter(description = "Identificador único del certamen", example = "1", required = true)
            @PathVariable Long torneoId) {
        log.info("Petición recibida para consultar cronograma del torneo #{}", torneoId);
        return ResponseEntity.ok(cronogramaTorneoService.obtenerCronogramaTorneo(torneoId));
    }

    @Operation(
            summary = "Consultar partidos pendientes para delegados",
            description = "Lista los partidos suspendidos o reprogramados de un torneo, con filtro opcional por equipo para facilitar el seguimiento logístico al delegado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de partidos pendientes obtenido exitosamente"),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Torneo no encontrado (RFC 7807 ProblemDetail)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/partidos-pendientes")
    public ResponseEntity<List<PartidoPendienteDTO>> obtenerPartidosPendientes(
            @Parameter(description = "Identificador único del certamen", example = "1", required = true)
            @PathVariable Long torneoId,
            @Parameter(description = "Filtro opcional por identificador de equipo del delegado", example = "1")
            @RequestParam(required = false) Long equipoId) {
        log.info("Petición recibida para consultar partidos pendientes del torneo #{} (filtro equipoId={})", torneoId, equipoId);
        return ResponseEntity.ok(cronogramaTorneoService.obtenerPartidosPendientes(torneoId, equipoId));
    }
}
