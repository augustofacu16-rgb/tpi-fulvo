package com.fulvo.clima.controller;

import com.fulvo.clima.config.OpenApiConfig;
import com.fulvo.clima.service.ClimaService;
import com.fulvo.common.dto.ClimaPronosticoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Controlador REST para consultar pronósticos y evaluación de severidad climática (FN-08, RN-07).
 */
@Validated
@RestController
@RequestMapping("/v1/clima")
@RequiredArgsConstructor
@Tag(name = "Clima", description = "Adaptador de pronóstico y evaluación de severidad meteorológica (Open-Meteo)")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class ClimaController {

    private final ClimaService climaService;

    @Operation(
            summary = "Consultar pronóstico meteorológico y severidad",
            description = "Consulta la previsión climática (temperatura, precipitaciones, viento, tormenta) para una sede y horario " +
                    "determinados, evaluando si las condiciones impiden el inicio o continuidad del partido (FN-08, RN-07)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pronóstico y evaluación de severidad calculados exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClimaPronosticoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de consulta inválidos o fuera de rango geográfico (RFC 7807)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado o token ausente/inválido (RFC 7807)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado por rol insuficiente (RFC 7807)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Inconsistencia de negocio en fecha/hora o ubicación (RFC 7807)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio externo Open-Meteo no disponible o circuito abierto (RFC 7807)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/pronosticos")
    public ResponseEntity<ClimaPronosticoDTO> obtenerPronostico(
            @Parameter(description = "Latitud geográfica (-90 a 90)", example = "-31.4201", required = true)
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitud,

            @Parameter(description = "Longitud geográfica (-180 a 180)", example = "-64.1888", required = true)
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitud,

            @Parameter(description = "Fecha y hora del evento deportivo en formato ISO-8601", example = "2026-11-20T17:00:00", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora) {
        return ResponseEntity.ok(climaService.evaluarPronostico(latitud, longitud, fechaHora));
    }
}
