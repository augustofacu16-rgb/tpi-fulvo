package com.fulvo.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DescalificarEquiposRequest(
        @NotNull @Positive Long equipoLocalId,
        @NotNull @Positive Long equipoVisitanteId
) {
}
