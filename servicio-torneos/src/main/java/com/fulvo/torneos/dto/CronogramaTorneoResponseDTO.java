package com.fulvo.torneos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que consolida la visión del cronograma general de un certamen y sus contingencias (Subsección 4.5).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronogramaTorneoResponseDTO {

    private Long torneoId;
    private String nombreTorneo;
    private String estadoTorneo;
    private int totalPartidosAfectados;
    private int partidosSuspendidos;
    private int partidosReprogramados;
    private List<NovedadCronogramaResponseDTO> novedades;
}
