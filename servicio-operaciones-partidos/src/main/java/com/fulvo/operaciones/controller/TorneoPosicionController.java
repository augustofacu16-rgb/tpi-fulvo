package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.GoleadorResponse;
import com.fulvo.operaciones.dto.PosicionResponse;
import com.fulvo.operaciones.service.TablaPosicionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/torneos")
@RequiredArgsConstructor
@Tag(name = "Posiciones y goleadores", description = "Consultas de estadísticas de los torneos")
@SecurityRequirement(name = "bearerAuth")
public class TorneoPosicionController {

    private final TablaPosicionService tablaPosicionService;

    @GetMapping("/{torneoId}/posiciones")
    @Operation(summary = "Consultar la tabla de posiciones",
            description = """
                    Devuelve las posiciones persistidas del torneo, ordenadas por puntos,
                    diferencia de gol y goles a favor, todos de mayor a menor.
                    La consulta no recalcula las estadísticas. El recálculo contempla partidos
                    finalizados con resultado y cancelaciones de Liga, que computan una derrota
                    para ambos equipos sin puntos. Requiere un JWT válido, incluido el de fanático.
                    No verifica la existencia del torneo en Torneos: sin registros devuelve una lista vacía.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de posiciones; puede estar vacía",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PosicionResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Error inesperado al procesar la consulta",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<PosicionResponse>> obtenerPosiciones(
            @Parameter(description = "Identificador lógico del torneo", example = "1") @PathVariable Long torneoId) {

        return ResponseEntity.ok(
                tablaPosicionService.obtenerPosiciones(torneoId)
        );
    }

    @GetMapping("/{torneoId}/goleadores")
    @Operation(summary = "Consultar los goleadores del torneo",
            description = """
                    Cuenta exclusivamente actuaciones GOL de partidos FINALIZADO y agrupa por
                    equipo y dorsal. Ordena por goles de mayor a menor, sin desempate adicional.
                    Enriquece nombre y apellido consultando Torneos; si no obtiene al jugador,
                    devuelve nombre Jugador y apellido Dorsal seguido del número.
                    Requiere un JWT válido, incluido el de fanático. Sin goles registrados devuelve
                    una lista vacía; no verifica la existencia del torneo.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de goleadores; puede estar vacía o incluir nombres de respaldo",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GoleadorResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Error inesperado al procesar la consulta",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<GoleadorResponse>> obtenerGoleadores(
            @Parameter(description = "Identificador lógico del torneo", example = "1") @PathVariable Long torneoId) {

        return ResponseEntity.ok(
                tablaPosicionService.obtenerGoleadores(torneoId)
        );
    }
}
