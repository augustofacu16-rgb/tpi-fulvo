package com.fulvo.torneos.dto;

import com.fulvo.torneos.domain.FormatoTorneo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearTorneoDTO {

    @NotBlank(message = "El nombre del torneo es obligatorio")
    private String nombre;

    @NotNull(message = "El formato del torneo es obligatorio (LIGA o LLAVES)")
    private FormatoTorneo formato;

    @NotNull(message = "El cupo de equipos es obligatorio")
    private Integer cupoEquipos;

    private LocalDateTime fechaInicio;
}
