package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.CambiarEstadoTorneoDTO;
import com.fulvo.torneos.dto.CrearTorneoDTO;
import com.fulvo.torneos.dto.FixtureGeneradoResponseDTO;
import com.fulvo.torneos.dto.TorneoResponseDTO;
import com.fulvo.torneos.service.TorneoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.fulvo.common.dto.JugadorPlantelDTO;

import java.net.URI;
import java.util.List;

@Tag(name = "Torneos", description = "Endpoints para la creación, consulta y gestión del ciclo de vida de torneos")
@RestController
@RequestMapping("/v1/torneos")
@RequiredArgsConstructor
@Slf4j
public class TorneoController {

    private final TorneoService torneoService;

    @Operation(summary = "Crear un nuevo torneo", description = "Crea un certamen validando cupos estrictos (10/20 Liga, 8/16 Llaves)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Torneo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Cupo o datos de entrada inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere ROL_ORGANIZADOR")
    })
    @PostMapping
    public ResponseEntity<TorneoResponseDTO> crearTorneo(@Valid @RequestBody CrearTorneoDTO dto) {
        TorneoResponseDTO creado = torneoService.crearTorneo(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @Operation(summary = "Listar torneos", description = "Retorna todos los torneos registrados con la cantidad de equipos inscriptos")
    @GetMapping
    public ResponseEntity<List<TorneoResponseDTO>> listarTorneos() {
        return ResponseEntity.ok(torneoService.listarTorneos());
    }

    @Operation(summary = "Obtener detalle de un torneo", description = "Consulta un torneo por su identificador único")
    @GetMapping("/{id}")
    public ResponseEntity<TorneoResponseDTO> obtenerTorneoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerTorneoPorId(id));
    }

    @Operation(summary = "Cambiar estado de un torneo", description = "Cierra la inscripción al 100% o cancela el certamen")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado de torneo actualizado exitosamente"),
        @ApiResponse(responseCode = "409", description = "Conflicto: cupo incompleto para cierre de inscripción")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TorneoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoTorneoDTO dto) {
        return ResponseEntity.ok(torneoService.cambiarEstado(id, dto));
    }

    @Operation(summary = "Generar fixture del torneo", description = "Genera el fixture determinístico (Round Robin o Llaves) y despacha a Operaciones")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fixture generado y despachado a Operaciones"),
        @ApiResponse(responseCode = "409", description = "Conflicto: cupo incompleto o torneo ya iniciado")
    })
    @PostMapping("/{id}/fixtures")
    public ResponseEntity<FixtureGeneradoResponseDTO> generarFixture(@PathVariable Long id) {
        FixtureGeneradoResponseDTO fixture = torneoService.generarFixture(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(fixture);
    }
    @Operation(summary = "Listar jugadores de los planteles activos de un torneo")
    @GetMapping("/{id}/jugadores")
    public ResponseEntity<List<JugadorPlantelDTO>> obtenerJugadoresTorneo(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerJugadoresPlantel(id));
    }
}
