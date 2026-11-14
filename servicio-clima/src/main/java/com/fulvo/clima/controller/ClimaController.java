package com.fulvo.clima.controller;

import com.fulvo.clima.service.ClimaService;
import com.fulvo.common.dto.ClimaPronosticoDTO;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
public class ClimaController {

    private final ClimaService climaService;

    @GetMapping("/pronosticos")
    public ResponseEntity<ClimaPronosticoDTO> obtenerPronostico(
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitud,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitud,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora) {
        return ResponseEntity.ok(climaService.evaluarPronostico(latitud, longitud, fechaHora));
    }
}
