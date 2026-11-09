package com.fulvo.operaciones.controller;

import com.fulvo.common.dto.CrucePartidoDTO;
import com.fulvo.operaciones.service.PartidoLoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1/partidos")
public class PartidoLoteController {

    private final PartidoLoteService service;


    public PartidoLoteController(PartidoLoteService service) {
        this.service = service;
    }

    @PostMapping("/lote")
    public ResponseEntity<Map<String, Object>> recibirLote(
            @RequestBody @NotEmpty List<@NotNull @Valid CrucePartidoDTO> cruces) {
        service.procesarLote(cruces);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("recibidos", cruces.size()));
    }
}
