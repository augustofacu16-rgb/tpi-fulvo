package com.fulvo.operaciones.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tabla_posicion", schema="esquema_operaciones")
@Getter
@NoArgsConstructor
public class TablaPosicion {
    public TablaPosicion(Long torneoId, Long equipoId) {
        this.torneoId = torneoId;
        this.equipoId = equipoId;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="torneo_id", nullable = false)
    private Long torneoId;

    @Column(name="equipo_id", nullable = false)
    private Long equipoId;

    @Column(name="partidos_jugados")
    private Integer partidosJugados = 0;

    @Column(name="partidos_ganados")
    private Integer partidosGanados = 0;

    @Column(name="partidos_empatados")
    private Integer partidosEmpatados = 0;

    @Column(name="partidos_perdidos")
    private Integer partidosPerdidos = 0;

    @Column(name="goles_a_favor")
    private Integer golesAFavor = 0;

    @Column(name="goles_en_contra")
    private Integer golesEnContra = 0;

    @Column(name="diferencia_gol")
    private Integer diferenciaGol = 0;

    @Column(name="puntos")
    private Integer puntos = 0;

    public void reiniciarEstadisticas() {
        partidosJugados = 0;
        partidosGanados = 0;
        partidosEmpatados = 0;
        partidosPerdidos = 0;
        golesAFavor = 0;
        golesEnContra = 0;
        diferenciaGol = 0;
        puntos = 0;
    }

    public void registrarVictoria(int golesFavor, int golesContra) {
        registrarPartido(golesFavor, golesContra);
        partidosGanados++;
        puntos += 3;
    }

    public void registrarEmpate(int golesFavor, int golesContra) {
        if (golesFavor != golesContra) {
            throw new IllegalArgumentException("Un empate requiere la misma cantidad de goles");
        }
        registrarPartido(golesFavor, golesContra);
        partidosEmpatados++;
        puntos++;
    }

    public void registrarDerrota(int golesFavor, int golesContra) {
        registrarPartido(golesFavor, golesContra);
        partidosPerdidos++;
    }

    public void registrarDerrotaPorIncidentes() {
        partidosJugados++;
        partidosPerdidos++;
    }

    private void registrarPartido(int golesFavor, int golesContra) {
        if (golesFavor < 0 || golesContra < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos");
        }
        partidosJugados++;
        golesAFavor += golesFavor;
        golesEnContra += golesContra;
        diferenciaGol = golesAFavor - golesEnContra;
    }
}
