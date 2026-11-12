package com.fulvo.torneos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta de un reemplazo de jugador exitoso (Subsección 3.4.1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReemplazoJugadorResponseDTO {

    private Long equipoId;
    private String equipoNombre;
    private Long jugadorSalienteId;
    private String dniSaliente;
    private Long nuevoJugadorId;
    private String dniEntrante;
    private Integer cambiosRealizados;
    private Integer cambiosRestantes;
}
