package com.fulvo.torneos.dto;

import com.fulvo.torneos.domain.EstadoEquipo;
import com.fulvo.torneos.domain.EstadoTorneo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta de DELETE /v1/torneos/{idTorneo}/equipos/{idEquipo} (FN-13, Subsección 3.4.2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BajaEquipoResponseDTO {

    private Long equipoId;
    private String equipoNombre;
    private Long torneoId;
    private EstadoEquipo estadoEquipo;
    private EstadoTorneo estadoTorneo;
    private String motivo;
}
