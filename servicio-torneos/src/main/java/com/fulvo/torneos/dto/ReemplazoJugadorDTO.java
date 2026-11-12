package com.fulvo.torneos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuerpo de POST /v1/equipos/{id}/reemplazos-jugadores (FN-11, Subsección 3.4.1).
 * Identifica al jugador saliente por su ID autogenerado (Long PK) conforme a DISENO_SISTEMA_FULVO.md.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReemplazoJugadorDTO {

    @NotNull(message = "El ID del jugador saliente es obligatorio")
    private Long jugadorSalienteId;

    @NotNull(message = "Los datos del nuevo jugador son obligatorios")
    @Valid
    private JugadorInscripcionDTO nuevoJugador;
}
