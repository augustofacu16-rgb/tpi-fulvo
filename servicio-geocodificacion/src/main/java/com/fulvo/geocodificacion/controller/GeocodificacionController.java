package com.fulvo.geocodificacion.controller;

import com.fulvo.common.dto.GeocodificacionDTO;
import com.fulvo.geocodificacion.service.GeocodificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/geocodificaciones")
@RequiredArgsConstructor
public class GeocodificacionController {

    private final GeocodificacionService geocodificacionService;

    @PostMapping
    public ResponseEntity<GeocodificacionDTO> resolver(
            @Valid @RequestBody GeocodificacionDTO solicitud
    ) {
        GeocodificacionDTO resultado = geocodificacionService.resolver(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
