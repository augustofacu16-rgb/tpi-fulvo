package com.fulvo.torneos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearSolicitudInscripcionDTO {

    @NotBlank(message = "El nombre del equipo es obligatorio")
    private String nombreEquipo;

    @NotEmpty(message = "La lista de jugadores es obligatoria")
    @Valid
    private List<JugadorInscripcionDTO> jugadores;
}
