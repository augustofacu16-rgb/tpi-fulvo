package com.fulvo.operaciones.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CrearSedeRequest {

    @NotBlank
    @Size(max = 255)
    private String nombre;

    @NotBlank
    @Size(min = 5, max = 250)
    private String direccion;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitudManual;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitudManual;

    @AssertTrue(message = "Debe indicar ambas coordenadas manuales o ninguna")
    public boolean isCoordenadasManualesCompletas() {
        return (latitudManual == null) == (longitudManual == null);
    }
}