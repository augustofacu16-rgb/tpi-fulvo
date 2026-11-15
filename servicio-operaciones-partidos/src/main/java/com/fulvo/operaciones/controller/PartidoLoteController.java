package com.fulvo.operaciones.controller;

import com.fulvo.common.dto.CrucePartidoDTO;
import com.fulvo.operaciones.service.PartidoLoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la recepción y persistencia transaccional en lote de cruces de partidos (Subsección 2.3).
 * Documentado formalmente con OpenAPI 3.0 / Swagger (Subsección 2.6).
 */
@RestController
@RequestMapping("v1/partidos")
@Tag(name = "Partidos", description = "Asignación de recursos, arbitraje y contingencias climáticas de partidos")
@SecurityRequirement(name = "bearerAuth")
public class PartidoLoteController {

    private final PartidoLoteService service;

    public PartidoLoteController(PartidoLoteService service) {
        this.service = service;
    }

    @PostMapping("/lote")
    @Operation(
            summary = "Recepción y persistencia de lote de partidos",
            description = """
                    Recibe una lista no vacía de cruces y los persiste en estado PROGRAMADO en una
                    única transacción: si falla la persistencia, se revierte todo el lote.
                    torneoId, formatoTorneo y fechaOFase son obligatorios. El formato debe ser
                    LIGA o LLAVES. Los identificadores de equipo, si se informan, deben ser positivos;
                    pueden omitirse para cruces de fases futuras de LLAVES. fechaHora es opcional.
                    Aunque el DTO compartido incluye sedeId, arbitroDni y estado, este alta no aplica
                    esos campos: la sede y el árbitro se asignan por el endpoint de asignaciones y
                    el estado inicial siempre es PROGRAMADO.
                    Los identificadores de torneo y equipos son referencias lógicas: no se comprueba
                    su existencia en Torneos. No hay deduplicación: repetir la petición crea nuevos
                    partidos. El 202 se devuelve después de persistir; no implica procesamiento asíncrono.
                    A través del API Gateway requiere el rol organizador.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Lote de partidos aceptado y persistido exitosamente en estado PROGRAMADO",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", requiredProperties = {"recibidos"},
                                    additionalPropertiesSchema = Integer.class,
                                    description = "Objeto con recibidos: cantidad de partidos persistidos en esta petición"),
                            examples = @ExampleObject(value = "{\"recibidos\": 4}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "JSON ilegible, lote vacío, elementos nulos o cruces que incumplen las validaciones",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El API Gateway rechaza el acceso por rol insuficiente",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error inesperado al procesar o persistir el lote",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<Map<String, Object>> recibirLote(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Lista no vacía de cruces. fechaOFase representa la fecha de Liga o el número de fase de LLAVES",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(minItems = 1,
                                    schema = @Schema(implementation = CrucePartidoDTO.class)),
                            examples = {
                                    @ExampleObject(name = "Liga", summary = "Un cruce de Liga sin horario asignado",
                                            value = """
                                                    [{"torneoId":1,"formatoTorneo":"LIGA","fechaOFase":1,
                                                      "equipoLocalId":1,"equipoVisitanteId":2}]
                                                    """),
                                    @ExampleObject(name = "Llaves", summary = "Cruce inicial y cruce futuro con equipos pendientes",
                                            value = """
                                                    [{"torneoId":3,"formatoTorneo":"LLAVES","fechaOFase":1,
                                                      "equipoLocalId":14,"equipoVisitanteId":21},
                                                     {"torneoId":3,"formatoTorneo":"LLAVES","fechaOFase":2,
                                                      "equipoLocalId":null,"equipoVisitanteId":null}]
                                                    """)
                            }))
            @RequestBody @NotEmpty List<@NotNull @Valid CrucePartidoDTO> cruces) {
        service.procesarLote(cruces);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("recibidos", cruces.size()));
    }
}
