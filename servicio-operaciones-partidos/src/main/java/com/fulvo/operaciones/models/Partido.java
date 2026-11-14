package com.fulvo.operaciones.models;

import com.fulvo.operaciones.models.strategy.EstrategiaFormatoPartido;
import com.fulvo.operaciones.models.strategy.EstrategiasFormatoPartido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

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
    @Setter(AccessLevel.NONE)
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

    public void iniciar() {
        comportamientoActual().iniciar(this);
    }

    public void cancelarPorIncidentes() {
        comportamientoActual().cancelarPorIncidentes(this);
    }

    public void validarEquipoDelPartido(Long equipoId) {
        if (equipoId == null || (!equipoId.equals(equipoLocalId)
                && !equipoId.equals(equipoVisitanteId))) {
            throw new IllegalArgumentException(
                    "La actuación pertenece a un equipo que no participa del partido");
        }
    }

    public void validarFinalizacion(ResultadoPartido resultado) {
        comportamientoActual().validarFinalizacion(this, resultado);
    }

    public void finalizar(ResultadoPartido resultado) {
        comportamientoActual().finalizar(this, resultado);
    }

    void registrarResultado(ResultadoPartido resultado, Long ganador) {
        golesLocal = resultado.golesLocal();
        golesVisitante = resultado.golesVisitante();
        penalesLocal = resultado.penalesLocal();
        penalesVisitante = resultado.penalesVisitante();
        ganadorEquipoId = ganador;
    }

    public void incorporarClasificado(Long equipoId, boolean comoLocal) {
        comportamientoActual().incorporarClasificado(this, equipoId, comoLocal);
    }

    public void resolverAvanceSinRival() {
        comportamientoActual().resolverAvanceSinRival(this);
    }

    private EstadoDelPartido comportamientoActual() {
        return EstadoDelPartido.para(estado);
    }

    void transicionarA(EstadoPartido destino) {
        estado = destino;
    }

    EstrategiaFormatoPartido estrategiaFormato() {
        return EstrategiasFormatoPartido.para(formatoTorneo);
    }
}
