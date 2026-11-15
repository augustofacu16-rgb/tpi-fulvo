package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Goles de un jugador agrupados por equipo y dorsal en partidos finalizados")
public record GoleadorResponse(
        @Schema(description = "Identificador lógico del equipo del jugador", example = "1")
        Long equipoId,
        @Schema(description = "Dorsal del jugador; junto con equipoId identifica la agrupación de goles", example = "9")
        Integer dorsalJugador,
        @Schema(description = "Nombre obtenido desde Torneos; Jugador si no se pudo resolver", example = "Nicolás")
        String nombreJugador,
        @Schema(description = "Apellido obtenido desde Torneos; Dorsal seguido del número si no se pudo resolver", example = "Pérez")
        String apellidoJugador,
        @Schema(description = "Cantidad de actuaciones GOL en partidos FINALIZADO del torneo", example = "3")
        Integer goles
) {
}
