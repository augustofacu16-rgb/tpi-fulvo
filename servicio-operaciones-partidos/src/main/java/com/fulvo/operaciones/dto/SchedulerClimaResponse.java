package com.fulvo.operaciones.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumen de ejecución del scheduler preventivo climático a 24 horas (FN-08, RN-07).
 */
public record SchedulerClimaResponse(
        LocalDateTime fechaEjecucion,
        int partidosEvaluados,
        int partidosSuspendidos,
        List<Long> idsPartidosSuspendidos
) {
}
