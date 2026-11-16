package com.fulvo.torneos.controller;

import com.fulvo.common.dto.JugadorPlantelDTO;
import com.fulvo.torneos.dto.CambiarEstadoTorneoDTO;
import com.fulvo.torneos.dto.CrearTorneoDTO;
import com.fulvo.torneos.dto.FixtureGeneradoResponseDTO;
import com.fulvo.torneos.dto.TorneoResponseDTO;
import com.fulvo.torneos.service.TorneoService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Torneos", description = "Endpoints para la creación, consulta, fixture y gestión del ciclo de vida de certámenes")
@RestController
@RequestMapping("/v1/torneos")
@RequiredArgsConstructor
@Slf4j
public class TorneoController {

    private final TorneoService torneoService;

    @Operation(
            summary = "Crear un nuevo certamen deportivo",
            description = "Registra un torneo en estado ABIERTO_INSCRIPCION validando cupos reglamentarios estrictos: exactamente 10 o 20 para Liga, y 8 o 16 para Llaves (RN-01). Requiere ROL_ORGANIZADOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Torneo creado exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cupo o datos de entrada inválidos (RFC 7807 ProblemDetail)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado o token JWT ausente/inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado: requiere rol ROL_ORGANIZADOR",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
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

    @Operation(
            summary = "Listar torneos registrados",
            description = "Retorna el catálogo completo de torneos con sus estados y la cantidad de equipos admitidos. Abierto a cualquier usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de torneos obtenido exitosamente"),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<TorneoResponseDTO>> listarTorneos() {
        return ResponseEntity.ok(torneoService.listarTorneos());
    }

    @Operation(
            summary = "Consultar detalle de un torneo",
            description = "Obtiene los datos detallados de un certamen mediante su identificador numérico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Torneo encontrado exitosamente"),
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
    @GetMapping("/{id}")
    public ResponseEntity<TorneoResponseDTO> obtenerTorneoPorId(
            @Parameter(description = "Identificador único del certamen", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerTorneoPorId(id));
    }

    @Operation(
            summary = "Modificar estado del torneo (Cierre o Cancelación)",
            description = "Permite al Organizador pasar el torneo a CERRADO_INSCRIPCION (validando el 100% del cupo cubierto, RN-01) o a CANCELADO en cualquier momento (RN-04)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado del certamen actualizado exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado no permitido o datos inválidos (RFC 7807 ProblemDetail)",
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
                    description = "Torneo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: cupo incompleto para cierre de inscripción o transición inválida (RN-01)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TorneoResponseDTO> cambiarEstado(
            @Parameter(description = "Identificador único del torneo", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoTorneoDTO dto) {
        return ResponseEntity.ok(torneoService.cambiarEstado(id, dto));
    }

    @Operation(
            summary = "Generar fixture determinístico y despachar a Operaciones",
            description = "Calcula los emparejamientos determinísticos (Round Robin para Liga, Llaves determinísticas para Copa) con el 100% de cupo admitido. Transiciona el torneo a INICIADO y despacha el lote de partidos vía RestClient hacia servicio-operaciones-partidos (POST /v1/partidos/lote). Exclusivo ROL_ORGANIZADOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fixture generado y despachado a Operaciones exitosamente"),
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
                    description = "Torneo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: cupo de equipos incompleto o fixture ya generado previamente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/{id}/fixtures")
    public ResponseEntity<FixtureGeneradoResponseDTO> generarFixture(
            @Parameter(description = "Identificador único del torneo", example = "1", required = true)
            @PathVariable Long id) {
        FixtureGeneradoResponseDTO fixture = torneoService.generarFixture(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(fixture);
    }

    @Operation(
            summary = "Listar jugadores de los planteles activos de un certamen",
            description = "Retorna el listado de futbolistas activos de todos los equipos del torneo para la conformación de actas y cómputo de estadísticas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de jugadores del plantel obtenido"),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Torneo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}/jugadores")
    public ResponseEntity<List<JugadorPlantelDTO>> obtenerJugadoresTorneo(
            @Parameter(description = "Identificador único del certamen", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerJugadoresPlantel(id));
    }
}
