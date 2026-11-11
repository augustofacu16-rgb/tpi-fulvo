package com.fulvo.torneos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoTorneoDTO {

    @NotBlank(message = "El estado del torneo es obligatorio")
    private String estado;
}
