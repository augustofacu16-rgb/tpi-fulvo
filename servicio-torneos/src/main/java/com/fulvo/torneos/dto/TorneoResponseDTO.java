package com.fulvo.torneos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fulvo.torneos.domain.EstadoTorneo;
import com.fulvo.torneos.domain.FormatoTorneo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Detalle consolidado del estado de un certamen deportivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TorneoResponseDTO {

    @Schema(description = "Identificador único del torneo", example = "1")
    private Long id;

    @Schema(description = "Nombre oficial del certamen", example = "Torneo Apertura 2026")
    private String nombre;

    @Schema(description = "Formato de competencia", example = "LIGA")
    private FormatoTorneo formato;

    @Schema(description = "Cupo reglamentario de equipos admitidos", example = "10")
    private Integer cupoEquipos;

    @Schema(description = "Cantidad actual de equipos inscriptos y activos", example = "10")
    private Integer equiposInscriptos;

    @Schema(description = "Estado operativo actual del torneo", example = "INICIADO")
    private EstadoTorneo estado;

    @Schema(description = "Fecha de inicio prevista", example = "2026-04-01T09:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @Schema(description = "Fecha de creación del certamen en el sistema", example = "2026-03-15T18:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
