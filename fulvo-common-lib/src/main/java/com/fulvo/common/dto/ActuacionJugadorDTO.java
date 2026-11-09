package com.fulvo.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotNull(message = "El identificador del equipo es obligatorio")
    private Long equipoId;

    /**
     * Número de dorsal del jugador informado.
     */
    @NotNull(message = "El dorsal del jugador es obligatorio")
    @Min(value = 1, message = "El dorsal mínimo permitido es 1")
    @Max(value = 99, message = "El dorsal máximo permitido es 99")
    private Integer dorsalJugador;

    /**
     * Tipo de incidencia: "GOL", "TARJETA_AMARILLA", "TARJETA_ROJA".
     */
    @NotBlank(message = "El tipo de incidencia es obligatorio")
    @Pattern(
        regexp = "^(GOL|TARJETA_AMARILLA|TARJETA_ROJA)$",
        message = "Tipo de incidencia inválido. Permitidos: GOL, TARJETA_AMARILLA, TARJETA_ROJA"
    )
    private String tipo;

    /**
     * Minuto de juego en el que ocurrió la incidencia (opcional).
     */
    @Min(value = 1, message = "El minuto mínimo es 1")
    @Max(value = 130, message = "El minuto máximo permitido es 130")
    private Integer minuto;
}
