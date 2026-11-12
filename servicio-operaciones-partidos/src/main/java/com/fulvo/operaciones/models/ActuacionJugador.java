package com.fulvo.operaciones.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="actuacion_jugador", schema="esquema_operaciones")
@Getter
@Setter
@NoArgsConstructor
public class ActuacionJugador {
    public ActuacionJugador(Partido partido, Long equipoId, Integer dorsalJugador,
                            TipoActuacion tipo) {
        this.partido = partido;
        this.equipoId = equipoId;
        this.dorsalJugador = dorsalJugador;
        this.tipo = tipo;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @Column(name="equipo_id", nullable = false)
    private Long equipoId;

    @Column(name="dorsal_jugador", nullable = false)
    private Integer dorsalJugador;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo", nullable = false)
    private TipoActuacion tipo;

}
