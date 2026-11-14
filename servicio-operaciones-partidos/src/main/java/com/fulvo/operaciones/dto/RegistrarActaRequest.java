package com.fulvo.operaciones.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegistrarActaRequest(
        @NotNull @Min(0) Integer golesLocal,
        @NotNull @Min(0) Integer golesVisitante,
        @Min(0) Integer penalesLocal,
        @Min(0) Integer penalesVisitante,
        @NotNull List<@Valid ActuacionJugadorRequest> actuaciones
) {
}