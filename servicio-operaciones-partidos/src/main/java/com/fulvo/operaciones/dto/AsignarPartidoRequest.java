package com.fulvo.operaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AsignarPartidoRequest(
        @NotNull
        Long sedeId,

        @NotBlank
        String arbitroDni,

        @NotNull
        LocalDateTime fechaHora
) {
}
