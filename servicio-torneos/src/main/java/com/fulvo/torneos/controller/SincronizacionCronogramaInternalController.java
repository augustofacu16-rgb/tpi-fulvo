package com.fulvo.torneos.controller;

import com.fulvo.common.dto.ReprogramacionPartidoDTO;
import com.fulvo.common.dto.SuspensionPartidoDTO;
import com.fulvo.torneos.dto.NovedadCronogramaResponseDTO;
import com.fulvo.torneos.service.CronogramaTorneoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST interno para la sincronización de contingencias de partidos
 * entre microservicios (servicio-operaciones-partidos -> servicio-torneos) (Subsección 4.5, RN-07, RN-12).
 */
@Slf4j
@RestController
@RequestMapping("/internal/v1/torneos/{torneoId}/partidos")
@RequiredArgsConstructor
@Tag(name = "Sincronización Interna Cronograma", description = "Endpoints internos para registrar contingencias de partidos y notificar delegados")
public class SincronizacionCronogramaInternalController {

    private final CronogramaTorneoService cronogramaTorneoService;

    @Operation(
            summary = "Registrar suspensión de partido",
            description = "Consume la notificación de suspensión de un partido (por lluvia severa o fuerza mayor) y dispara alertas a los delegados (RN-07, RN-12)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suspensión procesada y notificada a delegados"),
            @ApiResponse(responseCode = "404", description = "Torneo o equipo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/suspension")
    public ResponseEntity<NovedadCronogramaResponseDTO> registrarSuspension(
            @PathVariable Long torneoId,
            @Valid @RequestBody SuspensionPartidoDTO dto) {
        log.info("Petición interna recibida para suspender partido #{} en torneo #{}", dto.partidoId(), torneoId);
        NovedadCronogramaResponseDTO response = cronogramaTorneoService.registrarSuspension(torneoId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Registrar reprogramación de partido",
            description = "Consume la reasignación de fecha y sede para un partido suspendido y notifica a los delegados involucrados"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reprogramación registrada y notificada a delegados"),
            @ApiResponse(responseCode = "404", description = "Torneo o equipo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/reprogramacion")
    public ResponseEntity<NovedadCronogramaResponseDTO> registrarReprogramacion(
            @PathVariable Long torneoId,
            @Valid @RequestBody ReprogramacionPartidoDTO dto) {
        log.info("Petición interna recibida para reprogramar partido #{} en torneo #{}", dto.partidoId(), torneoId);
        NovedadCronogramaResponseDTO response = cronogramaTorneoService.registrarReprogramacion(torneoId, dto);
        return ResponseEntity.ok(response);
    }
}
