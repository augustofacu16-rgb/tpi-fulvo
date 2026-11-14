package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.AsignarPartidoRequest;
import com.fulvo.operaciones.dto.AsignarPartidoResponse;
import com.fulvo.operaciones.dto.ReprogramarPartidoRequest;
import com.fulvo.operaciones.dto.ReprogramarPartidoResponse;
import com.fulvo.operaciones.dto.SchedulerClimaResponse;
import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.service.ClimaSchedulerService;
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
    private final ClimaSchedulerService climaSchedulerService;

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

    @PostMapping("/scheduler-clima/ejecutar")
    public ResponseEntity<SchedulerClimaResponse> ejecutarSchedulerClima() {
        return ResponseEntity.ok(climaSchedulerService.evaluarPartidosProximas24Horas());
    }

    @PostMapping("/{id}/reprogramaciones")
    public ResponseEntity<ReprogramarPartidoResponse> reprogramar(
            @PathVariable Long id,
            @Valid @RequestBody ReprogramarPartidoRequest request) {

        Partido partido = partidoService.reprogramar(id, request);

        ReprogramarPartidoResponse response = new ReprogramarPartidoResponse(
                partido.getId(),
                partido.getSede().getId(),
                partido.getSede().getNombre(),
                partido.getFechaHora(),
                partido.getEstado()
        );

        return ResponseEntity.ok(response);
    }
}
