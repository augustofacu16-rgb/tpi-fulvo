package com.fulvo.operaciones.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name="actuacion_jugador", schema="esquema_operaciones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActuacionJugador {
    public ActuacionJugador(Partido partido, Long equipoId, Integer dorsalJugador,
                            TipoActuacion tipo) {
        if (partido == null) {
            throw new IllegalArgumentException("La actuación debe pertenecer a un partido");
        }
        if (equipoId == null || equipoId <= 0) {
            throw new IllegalArgumentException("El equipo de la actuación debe tener un identificador positivo");
        }
        if (dorsalJugador == null || dorsalJugador <= 0) {
            throw new IllegalArgumentException("El dorsal del jugador debe ser positivo");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de actuación es obligatorio");
        }
        partido.validarEquipoDelPartido(equipoId);
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
