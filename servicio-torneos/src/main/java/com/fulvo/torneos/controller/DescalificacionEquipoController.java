package com.fulvo.torneos.controller;

import com.fulvo.common.dto.DescalificarEquiposRequest;
import com.fulvo.torneos.service.EquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/torneos")
@RequiredArgsConstructor
public class DescalificacionEquipoController {

    private final EquipoService equipoService;

    @PostMapping("/{torneoId}/descalificaciones-incidentes")
    public ResponseEntity<Void> descalificarPorIncidentes(
            @PathVariable Long torneoId,
            @Valid @RequestBody DescalificarEquiposRequest request) {
        equipoService.descalificarPorIncidentes(torneoId, request);
        return ResponseEntity.noContent().build();
    }
}
