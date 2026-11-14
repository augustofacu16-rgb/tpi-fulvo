package com.fulvo.torneos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la persistencia y trazabilidad de eventos sobre el cronograma
 * de partidos (suspensiones y reprogramaciones) en esquema_torneos (Subsección 4.5, RN-07, RN-12).
 */
@Entity
@Table(name = "novedad_cronograma", schema = "esquema_torneos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadCronograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "torneo_id", nullable = false)
    private Long torneoId;

    @Column(name = "partido_id", nullable = false)
    private Long partidoId;

    @Column(name = "equipo_local_id", nullable = false)
    private Long equipoLocalId;

    @Column(name = "equipo_visitante_id", nullable = false)
    private Long equipoVisitanteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_partido", nullable = false, length = 30)
    private EstadoNovedadPartido estadoPartido;

    @Column(length = 255)
    private String motivo;

    @Column(name = "fecha_original")
    private LocalDateTime fechaOriginal;

    @Column(name = "nueva_fecha_hora")
    private LocalDateTime nuevaFechaHora;

    @Column(name = "nueva_sede_id")
    private Long nuevaSedeId;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "notificado_delegados", nullable = false)
    private boolean notificadoDelegados;
}
