package com.fulvo.operaciones.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="arbitro", schema="esquema_operaciones")
@Getter
@Setter
@NoArgsConstructor
public class Arbitro {
    public Arbitro(String dni, String usuarioId, String nombre, String apellido) {
        this.dni = dni;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.asignadoPartido = false;
    }

    @Id
    private String dni;

    @Column(name="usuario_id", nullable = false)
    private String usuarioId;

    @Column(name="nombre", nullable = false)
    private String nombre;

    @Column(name="apellido", nullable = false)
    private String apellido;

    @Column(name="asignado_partido")
    private Boolean asignadoPartido = false;
}
