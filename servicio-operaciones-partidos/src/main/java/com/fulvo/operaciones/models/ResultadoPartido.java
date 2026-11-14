package com.fulvo.operaciones.models;

public record ResultadoPartido(
        Integer golesLocal,
        Integer golesVisitante,
        Integer penalesLocal,
        Integer penalesVisitante
) {
    public ResultadoPartido {
        if (golesLocal == null || golesVisitante == null
                || golesLocal < 0 || golesVisitante < 0) {
            throw new IllegalArgumentException("Los goles de ambos equipos son obligatorios y no pueden ser negativos");
        }
        if ((penalesLocal != null && penalesLocal < 0)
                || (penalesVisitante != null && penalesVisitante < 0)) {
            throw new IllegalArgumentException("Los penales no pueden ser negativos");
        }
    }
}
