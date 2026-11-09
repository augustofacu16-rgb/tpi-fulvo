package com.fulvo.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
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
    @NotNull(message = "El identificador del partido es obligatorio")
    private Long partidoId;

    /**
     * DNI del árbitro que asienta el acta oficial.
     */
    @NotBlank(message = "El DNI del árbitro es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI del árbitro debe tener 7 u 8 dígitos numéricos")
    private String arbitroDni;

    /**
     * Cantidad total de goles convertidos por el equipo local en los 90 minutos.
     */
    @NotNull(message = "Los goles del equipo local son obligatorios")
    @PositiveOrZero(message = "Los goles del equipo local no pueden ser negativos")
    private Integer golesLocal;

    /**
     * Cantidad total de goles convertidos por el equipo visitante en los 90 minutos.
     */
    @NotNull(message = "Los goles del equipo visitante son obligatorios")
    @PositiveOrZero(message = "Los goles del equipo visitante no pueden ser negativos")
    private Integer golesVisitante;

    /**
     * Cantidad de penales convertidos por el equipo local (obligatorio en Llaves en caso de empate).
     */
    @PositiveOrZero(message = "Los penales del equipo local no pueden ser negativos")
    private Integer penalesLocal;

    /**
     * Cantidad de penales convertidos por el equipo visitante (obligatorio en Llaves en caso de empate).
     */
    @PositiveOrZero(message = "Los penales del equipo visitante no pueden ser negativos")
    private Integer penalesVisitante;

    /**
     * Detalle de goles y amonestaciones/expulsiones registradas por dorsal.
     */
    @NotNull(message = "La lista de actuaciones no puede ser nula")
    @Valid
    @Builder.Default
    private List<ActuacionJugadorDTO> actuaciones = new ArrayList<>();
}
