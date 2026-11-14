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
import com.fulvo.operaciones.dto.EstadoPartidoResponse;
import com.fulvo.operaciones.dto.ActaResponse;
import com.fulvo.operaciones.dto.RegistrarActaRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/v1/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final PartidoService partidoService;

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EstadoPartidoResponse> iniciarPartido(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Partido partido = partidoService.iniciarPartido(id, jwt);

        EstadoPartidoResponse response = new EstadoPartidoResponse(
                partido.getId(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<EstadoPartidoResponse> cancelarPartido(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Partido partido = partidoService.cancelarPartido(id, jwt);

        EstadoPartidoResponse response = new EstadoPartidoResponse(
                partido.getId(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }

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

    @PostMapping("/{id}/actas")
    public ResponseEntity<ActaResponse> registrarActa(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarActaRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Partido partido = partidoService.registrarActa(id, request, jwt);

        ActaResponse response = new ActaResponse(
                partido.getId(),
                partido.getEstado(),
                partido.getGolesLocal(),
                partido.getGolesVisitante(),
                partido.getPenalesLocal(),
                partido.getPenalesVisitante(),
                partido.getGanadorEquipoId()
        );

        return ResponseEntity.ok(response);
    }
}
