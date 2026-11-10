package com.fulvo.torneos.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_inscripcion", schema = "esquema_torneos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudInscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(name = "fanatico_usuario_id", nullable = false, length = 100)
    private String fanaticoUsuarioId;

    @Column(name = "nombre_equipo", nullable = false, length = 100)
    private String nombreEquipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSolicitudInscripcion estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @Column(name = "datos_plantel_json", columnDefinition = "TEXT")
    private String datosPlantelJson;

    @PrePersist
    public void prePersist() {
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoSolicitudInscripcion.PENDIENTE;
        }
    }
}
