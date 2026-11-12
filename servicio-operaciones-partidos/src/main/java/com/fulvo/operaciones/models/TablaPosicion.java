package com.fulvo.operaciones.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tabla_posicion", schema="esquema_operaciones")
@Getter
@Setter
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
}
