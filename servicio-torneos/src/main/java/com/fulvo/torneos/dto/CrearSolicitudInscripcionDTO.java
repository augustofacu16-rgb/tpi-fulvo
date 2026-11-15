package com.fulvo.torneos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Schema(description = "Payload para la postulación de un equipo con su lista de buena fe (RN-02, RN-03)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearSolicitudInscripcionDTO {

    @Schema(description = "Nombre oficial o distintivo del equipo postulante", example = "Los Halcones FC", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del equipo es obligatorio")
    private String nombreEquipo;

    @Schema(description = "Nómina de buena fe de entre 15 y 22 jugadores, con al menos un ARQUERO y un capitán único (RN-02)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "La lista de jugadores es obligatoria")
    @Valid
    private List<JugadorInscripcionDTO> jugadores;
}
