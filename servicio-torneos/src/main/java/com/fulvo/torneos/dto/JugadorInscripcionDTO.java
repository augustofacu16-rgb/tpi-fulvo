package com.fulvo.torneos.dto;

import com.fulvo.torneos.domain.PosicionJugador;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JugadorInscripcionDTO {

    @NotBlank(message = "El DNI del jugador es obligatorio")
    private String dni;

    @NotBlank(message = "El nombre del jugador es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido del jugador es obligatorio")
    private String apellido;

    @NotNull(message = "La posición del jugador es obligatoria")
    private PosicionJugador posicion;

    @NotNull(message = "El dorsal del jugador es obligatorio")
    private Integer dorsal;

    @Builder.Default
    private Boolean esCapitan = false;
}
