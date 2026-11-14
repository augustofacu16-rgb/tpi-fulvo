package com.fulvo.common.dto;

import java.io.Serializable;

public record JugadorPlantelDTO(
        Long equipoId,
        Integer dorsal,
        String nombre,
        String apellido
) implements Serializable {
}