package com.fulvo.operaciones.dto;

import com.fulvo.operaciones.models.TipoActuacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActuacionJugadorRequest(
        @NotNull Long equipoId,
        @NotNull @Positive Integer dorsalJugador,
        @NotNull TipoActuacion tipo
) {
}