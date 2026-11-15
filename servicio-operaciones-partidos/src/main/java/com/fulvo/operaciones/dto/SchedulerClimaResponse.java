package com.fulvo.operaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumen de ejecución del scheduler preventivo climático a 24 horas (FN-08, RN-07).
 */
@Schema(description = "Resumen de la evaluación climática. Un partido evaluado no necesariamente fue suspendido; los errores por partido se registran en logs y no detienen el lote",
        example = "{\"fechaEjecucion\":\"2030-12-10T08:00:00\",\"partidosEvaluados\":3,\"partidosSuspendidos\":1,\"idsPartidosSuspendidos\":[1]}")
public record SchedulerClimaResponse(
        @Schema(description = "Fecha y hora local de inicio de la evaluación",
                example = "2030-12-10T08:00:00")
        LocalDateTime fechaEjecucion,
        @Schema(description = "Cantidad de candidatos con sede y coordenadas en estado PROGRAMADO o REPROGRAMADO dentro de las próximas 24 horas",
                example = "3")
        int partidosEvaluados,
        @Schema(description = "Cantidad de suspensiones confirmadas localmente y sincronizadas con Torneos en esta ejecución",
                example = "1")
        int partidosSuspendidos,
        @Schema(description = "Identificadores de las suspensiones confirmadas en esta ejecución. Lista vacía si ninguna se confirmó",
                example = "[1]")
        List<Long> idsPartidosSuspendidos
) {
}
