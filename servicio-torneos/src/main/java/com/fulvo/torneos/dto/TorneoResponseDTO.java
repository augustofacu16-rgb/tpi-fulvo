package com.fulvo.torneos.dto;

import com.fulvo.torneos.domain.EstadoTorneo;
import com.fulvo.torneos.domain.FormatoTorneo;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TorneoResponseDTO {

    private Long id;
    private String nombre;
    private FormatoTorneo formato;
    private Integer cupoEquipos;
    private Integer equiposInscriptos;
    private EstadoTorneo estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCreacion;
}
