package com.fulvo.torneos.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jugador", schema = "esquema_torneos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String dni;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false)
    private Integer dorsal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PosicionJugador posicion;

    @Column(name = "es_capitan", nullable = false)
    @Builder.Default
    private Boolean esCapitan = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean suspendido = false;

    @PrePersist
    public void prePersist() {
        if (this.esCapitan == null) {
            this.esCapitan = false;
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.suspendido == null) {
            this.suspendido = false;
        }
    }
}
