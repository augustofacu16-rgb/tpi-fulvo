package com.fulvo.operaciones.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="sede", schema="esquema_operaciones")
@Getter
@Setter
@NoArgsConstructor
public class Sede {
    public Sede(String nombre, String direccion, Double latitud, Double longitud) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre", nullable = false)
    private String nombre;

    @Column(name="direccion", nullable = false)
    private String direccion;

    @Column(name="latitud")
    private Double latitud;

    @Column(name="longitud")
    private Double longitud;

}
