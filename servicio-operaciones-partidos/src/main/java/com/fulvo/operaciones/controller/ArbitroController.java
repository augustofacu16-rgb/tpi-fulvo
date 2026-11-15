package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.ArbitroResponse;
import com.fulvo.operaciones.dto.CrearArbitroRequest;
import com.fulvo.operaciones.models.Arbitro;
import com.fulvo.operaciones.service.ArbitroService;
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

@RestController
@RequestMapping("/v1/arbitros")
@RequiredArgsConstructor
@Tag(
        name = "Árbitros",
        description = "Registro de árbitros vinculados a usuarios de Keycloak"
)
@SecurityRequirement(name = "bearerAuth")
public class ArbitroController {

    private final ArbitroService arbitroService;

    @PostMapping
    @Operation(
            summary = "Registrar un árbitro",
            description = """
                    Registra un árbitro identificado por su DNI.
                    usuarioId debe corresponder al identificador sub del usuario de Keycloak;
                    esta operación no verifica su existencia en Keycloak.
                    A través del API Gateway requiere el rol organizador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Árbitro registrado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArbitroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Campos obligatorios ausentes o datos inválidos",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente, inválido o vencido",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El API Gateway rechaza el acceso por rol insuficiente",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe un árbitro con el DNI indicado",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<ArbitroResponse> crear(
            @Valid @RequestBody CrearArbitroRequest request) {

        Arbitro arbitro = arbitroService.crear(request);

        ArbitroResponse response = new ArbitroResponse(
                arbitro.getDni(),
                arbitro.getUsuarioId(),
                arbitro.getNombre(),
                arbitro.getApellido()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
