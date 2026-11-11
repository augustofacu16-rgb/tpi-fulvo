package com.fulvo.operaciones.controller;

import com.fulvo.common.dto.CrucePartidoDTO;
import com.fulvo.operaciones.service.PartidoLoteService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Partidos", description = "Endpoints de administración de partidos y recepción en lote de fixtures")
@SecurityRequirement(name = "bearerAuth")
public class PartidoLoteController {

    private final PartidoLoteService service;

    public PartidoLoteController(PartidoLoteService service) {
        this.service = service;
    }

    @PostMapping("/lote")
    @Operation(
            summary = "Recepción y persistencia de lote de partidos",
            description = "Recibe el fixture generado determinísticamente por servicio-torneos y persiste masivamente los encuentros en estado PROGRAMADO de forma transaccional y atómica."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Lote de partidos aceptado y persistido exitosamente en estado PROGRAMADO",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"recibidos\": 4}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Petición inválida: lote vacío o datos de cruces no válidos (RFC 7807)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado: falta cabecera Authorization Bearer JWT",
                    content = @Content(mediaType = "application/problem+json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado: rol insuficiente",
                    content = @Content(mediaType = "application/problem+json")
            )
    })
    public ResponseEntity<Map<String, Object>> recibirLote(
            @RequestBody @NotEmpty List<@NotNull @Valid CrucePartidoDTO> cruces) {
        service.procesarLote(cruces);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("recibidos", cruces.size()));
    }
}
