package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.BajaEquipoResponseDTO;
import com.fulvo.torneos.dto.ReemplazoJugadorDTO;
import com.fulvo.torneos.dto.ReemplazoJugadorResponseDTO;
import com.fulvo.torneos.service.EquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Equipos", description = "Gestión del plantel y de la permanencia de los equipos en el certamen")
@RestController
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    @Operation(summary = "Reemplazar un jugador del plantel",
            description = "Sustituye a un jugador vigente por otro (máximo 5 cambios por equipo; prohíbe sustituir suspendidos)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reemplazo realizado"),
        @ApiResponse(responseCode = "400", description = "Sexto cambio, jugador suspendido o datos del plantel inválidos"),
        @ApiResponse(responseCode = "404", description = "Equipo o jugador saliente inexistente"),
        @ApiResponse(responseCode = "409", description = "Torneo no iniciado, equipo inactivo o DNI entrante ya inscripto")
    })
    @PostMapping("/v1/equipos/{id}/reemplazos-jugadores")
    public ResponseEntity<ReemplazoJugadorResponseDTO> reemplazarJugador(
            @PathVariable("id") Long equipoId,
            @Valid @RequestBody ReemplazoJugadorDTO dto) {
        return ResponseEntity.ok(equipoService.reemplazarJugador(equipoId, dto));
    }

    @Operation(summary = "Baja voluntaria o descalificación de un equipo",
            description = "tipo=BAJA_VOLUNTARIA (por defecto) pasa el equipo a DADO_DE_BAJA; tipo=DESCALIFICACION lo pasa a DESCALIFICADO. Notifica al delegado por email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Equipo dado de baja"),
        @ApiResponse(responseCode = "400", description = "Tipo de baja inválido"),
        @ApiResponse(responseCode = "404", description = "Torneo inexistente o equipo ajeno al torneo"),
        @ApiResponse(responseCode = "409", description = "Equipo ya inactivo o torneo finalizado/cancelado")
    })
    @DeleteMapping("/v1/torneos/{idTorneo}/equipos/{idEquipo}")
    public ResponseEntity<BajaEquipoResponseDTO> darDeBajaEquipo(
            @PathVariable("idTorneo") Long torneoId,
            @PathVariable("idEquipo") Long equipoId,
            @RequestParam(value = "tipo", defaultValue = EquipoService.TIPO_BAJA_VOLUNTARIA) String tipo,
            @RequestParam(value = "motivo", required = false) String motivo) {
        return ResponseEntity.ok(equipoService.darDeBajaEquipo(torneoId, equipoId, tipo, motivo));
    }
}
