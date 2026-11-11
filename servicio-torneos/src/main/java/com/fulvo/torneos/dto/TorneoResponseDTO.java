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

    public TorneoResponseDTO(Long id, String nombre, FormatoTorneo formato, Integer cupoEquipos,
                             Long equiposInscriptos, EstadoTorneo estado,
                             LocalDateTime fechaInicio, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.formato = formato;
        this.cupoEquipos = cupoEquipos;
        this.equiposInscriptos = equiposInscriptos != null ? equiposInscriptos.intValue() : 0;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaCreacion = fechaCreacion;

    }
}
