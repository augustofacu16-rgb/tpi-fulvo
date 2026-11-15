package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estadísticas acumuladas de un equipo en el torneo")
public record PosicionResponse(
        @Schema(description = "Identificador lógico del equipo", example = "1")
        Long equipoId,
        @Schema(description = "Partidos computados, incluidas las cancelaciones por incidentes en Liga", example = "3")
        Integer partidosJugados,
        @Schema(description = "Cantidad de victorias", example = "1")
        Integer partidosGanados,
        @Schema(description = "Cantidad de empates", example = "1")
        Integer partidosEmpatados,
        @Schema(description = "Cantidad de derrotas, incluidas las cancelaciones por incidentes en Liga", example = "1")
        Integer partidosPerdidos,
        @Schema(description = "Goles anotados en los partidos computados", example = "4")
        Integer golesAFavor,
        @Schema(description = "Goles recibidos en los partidos computados", example = "3")
        Integer golesEnContra,
        @Schema(description = "Goles a favor menos goles en contra; puede ser negativa", example = "1")
        Integer diferenciaGol,
        @Schema(description = "Puntos acumulados: tres por victoria, uno por empate y cero por derrota", example = "4")
        Integer puntos
) {
}
