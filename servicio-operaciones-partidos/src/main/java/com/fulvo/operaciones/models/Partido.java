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
}
