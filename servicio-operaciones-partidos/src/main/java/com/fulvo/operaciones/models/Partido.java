package com.fulvo.operaciones.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "partido", schema = "esquema_operaciones")
@Getter
@Setter
@NoArgsConstructor
public class Partido {
    public Partido(Long torneoId, String formatoTorneo, Integer fechaOFase,
                   Long equipoLocalId, Long equipoVisitanteId, LocalDateTime fechaHora) {
        this.torneoId = torneoId;
        this.formatoTorneo = formatoTorneo;
        this.fechaOFase = fechaOFase;
        this.equipoLocalId = equipoLocalId;
        this.equipoVisitanteId = equipoVisitanteId;
        this.fechaHora = fechaHora;
        this.estado = EstadoPartido.PROGRAMADO;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "torneo_id", nullable = false)
    private Long torneoId;

    @Column(name = "formato_torneo", nullable = false)
    private String formatoTorneo;

    @Column(name = "fecha_o_fase", nullable = false)
    private Integer fechaOFase;

    @Column(name = "equipo_local_id")
    private Long equipoLocalId;

    @Column(name = "equipo_visitante_id")
    private Long equipoVisitanteId;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPartido estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id")
    private Sede sede;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitro_dni")
    private Arbitro arbitro;

    @Column(name = "goles_local")
    private Integer golesLocal;

    @Column(name = "goles_visitante")
    private Integer golesVisitante;

    @Column(name = "penales_local")
    private Integer penalesLocal;

    @Column(name = "penales_visitante")
    private Integer penalesVisitante;

    @Column(name = "ganador_equipo_id")
    private Long ganadorEquipoId;

    public String obtenerArbitroUsuarioId() {
        return this.arbitro.getUsuarioId();
    }
}
