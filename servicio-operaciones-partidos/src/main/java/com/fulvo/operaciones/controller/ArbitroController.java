package com.fulvo.operaciones.controller;

import com.fulvo.operaciones.dto.ArbitroResponse;
import com.fulvo.operaciones.dto.CrearArbitroRequest;
import com.fulvo.operaciones.models.Arbitro;
import com.fulvo.operaciones.service.ArbitroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/arbitros")
@RequiredArgsConstructor
public class ArbitroController {

    private final ArbitroService arbitroService;

    @PostMapping
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
