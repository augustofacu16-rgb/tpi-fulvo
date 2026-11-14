package com.fulvo.operaciones.dto;

public record PosicionResponse(
        Long equipoId,
        Integer partidosJugados,
        Integer partidosGanados,
        Integer partidosEmpatados,
        Integer partidosPerdidos,
        Integer golesAFavor,
        Integer golesEnContra,
        Integer diferenciaGol,
        Integer puntos
) {
}
