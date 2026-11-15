package com.fulvo.torneos.dto;

import com.fulvo.torneos.models.EstadoEquipo;
import com.fulvo.torneos.models.EstadoTorneo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta de DELETE /v1/torneos/{idTorneo}/equipos/{idEquipo} (FN-13, Subsección 3.4.2).
 */
@Schema(description = "Detalle de la baja voluntaria o descalificación de un equipo en un torneo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BajaEquipoResponseDTO {

    @Schema(description = "Identificador del equipo afectado", example = "1")
    private Long equipoId;

    @Schema(description = "Nombre oficial del equipo", example = "Los Halcones FC")
    private String equipoNombre;

    @Schema(description = "Identificador del torneo", example = "1")
    private Long torneoId;

    @Schema(description = "Nuevo estado del equipo: DADO_DE_BAJA o DESCALIFICADO", example = "DADO_DE_BAJA")
    private EstadoEquipo estadoEquipo;

    @Schema(description = "Estado actual del torneo tras la baja", example = "INICIADO")
    private EstadoTorneo estadoTorneo;

    @Schema(description = "Motivo de la baja o descalificación", example = "Baja voluntaria solicitada por el delegado")
    private String motivo;
}
