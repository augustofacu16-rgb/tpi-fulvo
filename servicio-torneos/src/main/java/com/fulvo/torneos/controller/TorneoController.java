package com.fulvo.torneos.controller;

import com.fulvo.torneos.dto.CrearTorneoDTO;
import com.fulvo.torneos.dto.TorneoResponseDTO;
import com.fulvo.torneos.service.TorneoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/torneos")
@RequiredArgsConstructor
@Slf4j
public class TorneoController {

    private final TorneoService torneoService;

    @PostMapping
    public ResponseEntity<TorneoResponseDTO> crearTorneo(@Valid @RequestBody CrearTorneoDTO dto) {
        TorneoResponseDTO creado = torneoService.crearTorneo(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<TorneoResponseDTO>> listarTorneos() {
        return ResponseEntity.ok(torneoService.listarTorneos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TorneoResponseDTO> obtenerTorneoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerTorneoPorId(id));
    }
}
