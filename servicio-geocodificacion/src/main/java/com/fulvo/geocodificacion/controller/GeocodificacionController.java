package com.fulvo.geocodificacion.controller;

import com.fulvo.common.dto.GeocodificacionDTO;
import com.fulvo.geocodificacion.config.OpenApiConfig;
import com.fulvo.geocodificacion.service.GeocodificacionService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el adaptador de geocodificación satelital.
 * Expone endpoints para resolución y normalización geográfica (Subsección 4.1 y 5.4).
 */
@RestController
@RequestMapping("/v1/geocodificaciones")
@RequiredArgsConstructor
@Tag(name = "Geocodificación", description = "Adaptador de resolución y normalización de coordenadas satelitales (Nominatim / OSM)")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class GeocodificacionController {

    private final GeocodificacionService geocodificacionService;

    @Operation(
            summary = "Resolver y normalizar geocodificación satelital",
            description = "Resuelve coordenadas (latitud/longitud) a partir de una dirección o normaliza la dirección " +
                    "a partir de coordenadas para sedes deportivas (FN-02, RN-05). Protegido mediante Circuit Breaker."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Geocodificación resuelta exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeocodificacionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida o campos faltantes (RFC 7807)",
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
                    description = "Error de regla de negocio o dirección inubicable (RFC 7807)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio externo Nominatim no disponible o circuito abierto (RFC 7807)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping({"", "/direcciones"})
    public ResponseEntity<GeocodificacionDTO> resolver(
            @Valid @RequestBody GeocodificacionDTO solicitud
    ) {
        GeocodificacionDTO resultado = geocodificacionService.resolver(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
