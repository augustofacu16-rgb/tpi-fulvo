package com.fulvo.operaciones.dto;

public record GoleadorResponse(
        Long equipoId,
        Integer dorsalJugador,
        String nombreJugador,
        String apellidoJugador,
        Integer goles
) {
}