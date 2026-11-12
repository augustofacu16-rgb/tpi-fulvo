package com.fulvo.operaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class CrearArbitroRequest {

    @NotBlank
    @Size(max = 255)
    private String dni;

    @NotBlank
    @Size(max = 255)
    private String usuarioId;

    @NotBlank
    @Size(max = 255)
    private String nombre;

    @NotBlank
    @Size(max = 255)
    private String apellido;

}
