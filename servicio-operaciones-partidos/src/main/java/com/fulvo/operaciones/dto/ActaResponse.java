package com.fulvo.operaciones.dto;

import com.fulvo.operaciones.models.EstadoPartido;

public record ActaResponse(
        Long partidoId,
        EstadoPartido estado,
        Integer golesLocal,
        Integer golesVisitante,
        Integer penalesLocal,
        Integer penalesVisitante,
        Long ganadorEquipoId
) {
}