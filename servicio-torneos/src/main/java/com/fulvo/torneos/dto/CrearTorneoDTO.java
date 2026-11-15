package com.fulvo.torneos.dto;

import com.fulvo.torneos.models.FormatoTorneo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Datos de entrada para el alta de un nuevo certamen deportivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearTorneoDTO {

    @Schema(description = "Nombre oficial del torneo", example = "Torneo Apertura 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del torneo es obligatorio")
    private String nombre;

    @Schema(description = "Formato de disputa: LIGA (Round Robin) o LLAVES (Eliminación directa)", example = "LIGA", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El formato del torneo es obligatorio (LIGA o LLAVES)")
    private FormatoTorneo formato;

    @Schema(description = "Cupo estricto de equipos: exactamente 10 o 20 para Liga; 8 o 16 para Llaves (RN-01)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El cupo de equipos es obligatorio")
    private Integer cupoEquipos;

    @Schema(description = "Fecha y hora estimada de inicio del torneo", example = "2026-04-01T09:00:00")
    private LocalDateTime fechaInicio;
}
