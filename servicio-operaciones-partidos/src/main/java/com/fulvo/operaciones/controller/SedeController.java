package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.CrearSedeRequest;
import com.fulvo.operaciones.dto.SedeResponse;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.service.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @PostMapping
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