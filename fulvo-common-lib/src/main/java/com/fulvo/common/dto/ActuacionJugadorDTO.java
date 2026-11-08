package com.fulvo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO para el registro individual de incidencias (goles, tarjetas) por dorsal en el acta arbitral.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActuacionJugadorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador lógico del equipo al que pertenece el jugador.
     */
    private Long equipoId;

    /**
     * Número de dorsal del jugador informado.
     */
    private Integer dorsalJugador;

    /**
     * Tipo de incidencia: "GOL", "TARJETA_AMARILLA", "TARJETA_ROJA".
     */
    private String tipo;

    /**
     * Minuto de juego en el que ocurrió la incidencia (opcional).
     */
    private Integer minuto;
}
