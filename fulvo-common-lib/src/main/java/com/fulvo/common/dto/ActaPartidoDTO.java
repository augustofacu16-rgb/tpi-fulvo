package com.fulvo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la carga y asentamiento del acta oficial de partido por el árbitro designado
 * (POST /v1/partidos/{id}/actas) según RN-08 y RN-10.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActaPartidoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador del partido al que corresponde el acta.
     */
    private Long partidoId;

    /**
     * DNI del árbitro que asienta el acta oficial.
     */
    private String arbitroDni;

    /**
     * Cantidad total de goles convertidos por el equipo local.
     */
    private Integer golesLocal;

    /**
     * Cantidad total de goles convertidos por el equipo visitante.
     */
    private Integer golesVisitante;

    /**
     * Cantidad de penales convertidos por el equipo local (obligatorio en Llaves en caso de empate).
     */
    private Integer penalesLocal;

    /**
     * Cantidad de penales convertidos por el equipo visitante (obligatorio en Llaves en caso de empate).
     */
    private Integer penalesVisitante;

    /**
     * Detalle de goles y amonestaciones/expulsiones registradas por dorsal.
     */
    @Builder.Default
    private List<ActuacionJugadorDTO> actuaciones = new ArrayList<>();
}
