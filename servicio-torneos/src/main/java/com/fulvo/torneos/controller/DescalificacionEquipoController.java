package com.fulvo.torneos.controller;

import com.fulvo.common.dto.DescalificarEquiposRequest;
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

@Tag(name = "Descalificaciones Internas", description = "Endpoints de comunicación interna inter-servicio para la descalificación de planteles (Subsección 3.2 y 5.2)")
@RestController
@RequestMapping("/internal/v1/torneos")
@RequiredArgsConstructor
public class DescalificacionEquipoController {

    private final EquipoService equipoService;

    @Operation(
            summary = "Descalificar equipos por incidentes graves en partido",
            description = "Endpoint interno invocado por servicio-operaciones-partidos cuando un árbitro cancela un encuentro por disturbios (RN-09). Descalifica a ambos planteles en Llaves de forma idempotente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ambos equipos descalificados exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de petición inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Torneo o equipo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/{torneoId}/descalificaciones-incidentes")
    public ResponseEntity<Void> descalificarPorIncidentes(
            @Parameter(description = "Identificador único del certamen", example = "1", required = true)
            @PathVariable Long torneoId,
            @Valid @RequestBody DescalificarEquiposRequest request) {
        equipoService.descalificarPorIncidentes(torneoId, request);
        return ResponseEntity.noContent().build();
    }
}
