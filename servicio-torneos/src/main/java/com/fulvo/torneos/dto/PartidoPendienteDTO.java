package com.fulvo.torneos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la trazabilidad y seguimiento de partidos pendientes o pospuestos para delegados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartidoPendienteDTO {

    private Long partidoId;
    private Long equipoLocalId;
    private String nombreEquipoLocal;
    private Long equipoVisitanteId;
    private String nombreEquipoVisitante;
    private String estadoPartido;
    private String motivo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaOriginal;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nuevaFechaHora;

    private Long nuevaSedeId;

    private boolean requiereReprogramacion;
}
