package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.CrearSedeRequest;
import com.fulvo.operaciones.dto.SedeResponse;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.service.SedeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/sedes")
@RequiredArgsConstructor
@Tag(name = "Sedes", description = "Registro de sedes y resolución de su ubicación geográfica")
@SecurityRequirement(name = "bearerAuth")
public class SedeController {

    private final SedeService sedeService;

    @PostMapping
    @Operation(
            summary = "Registrar una sede",
            description = """
                    Registra una sede resolviendo su dirección mediante el servicio de geocodificación.
                    Se priorizan las coordenadas del proveedor externo. Las coordenadas manuales
                    son opcionales, deben enviarse juntas y se utilizan como respaldo si el proveedor
                    no encuentra la dirección o no está disponible. Operaciones necesita comunicarse
                    con Geocodificación incluso cuando se envían coordenadas manuales.
                    A través del API Gateway requiere el rol organizador.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sede registrada con coordenadas resueltas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SedeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, coordenadas fuera de rango o par manual incompleto",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o vencido",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "El API Gateway rechaza el acceso por rol insuficiente",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dirección no encontrada y sin coordenadas manuales de respaldo",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "Fallo de comunicación o respuesta inválida de Geocodificación; también indisponibilidad del proveedor sin respaldo manual",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SedeResponse> crear(
            @Valid @RequestBody CrearSedeRequest request) {

        Sede sede = sedeService.crear(request);

        SedeResponse response = new SedeResponse(
                sede.getId(),
                sede.getNombre(),
                sede.getDireccion(),
                sede.getLatitud(),
                sede.getLongitud()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
