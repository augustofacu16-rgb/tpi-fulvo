package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.AsignarPartidoRequest;
import com.fulvo.operaciones.dto.AsignarPartidoResponse;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.service.PartidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final PartidoService partidoService;

    @PostMapping("/{id}/asignaciones")
    public ResponseEntity<AsignarPartidoResponse> asignar(
            @PathVariable Long id,
            @Valid @RequestBody AsignarPartidoRequest request) {

        Partido partido = partidoService.asignar(id, request);

        AsignarPartidoResponse response = new AsignarPartidoResponse(
                partido.getId(),
                request.sedeId(),
                request.arbitroDni(),
                partido.getFechaHora(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }
}
