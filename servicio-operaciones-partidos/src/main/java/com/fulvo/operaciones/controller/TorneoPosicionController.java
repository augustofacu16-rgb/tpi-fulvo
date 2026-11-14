package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.GoleadorResponse;
import com.fulvo.operaciones.dto.PosicionResponse;
import com.fulvo.operaciones.service.TablaPosicionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/torneos")
@RequiredArgsConstructor
public class TorneoPosicionController {

    private final TablaPosicionService tablaPosicionService;

    @GetMapping("/{torneoId}/posiciones")
    public ResponseEntity<List<PosicionResponse>> obtenerPosiciones(
            @PathVariable Long torneoId) {

        return ResponseEntity.ok(
                tablaPosicionService.obtenerPosiciones(torneoId)
        );
    }

    @GetMapping("/{torneoId}/goleadores")
    public ResponseEntity<List<GoleadorResponse>> obtenerGoleadores(
            @PathVariable Long torneoId) {

        return ResponseEntity.ok(
                tablaPosicionService.obtenerGoleadores(torneoId)
        );
    }
}
