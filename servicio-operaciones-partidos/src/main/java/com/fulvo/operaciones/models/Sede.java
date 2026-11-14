package com.fulvo.operaciones.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name="sede", schema="esquema_operaciones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sede {
    public Sede(String nombre, String direccion, Double latitud, Double longitud) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 255) {
            throw new IllegalArgumentException("El nombre de la sede es obligatorio y admite hasta 255 caracteres");
        }
        if (direccion == null || direccion.isBlank()
                || direccion.length() < 5 || direccion.length() > 250) {
            throw new IllegalArgumentException("La dirección debe tener entre 5 y 250 caracteres");
        }
        validarCoordenadas(latitud, longitud);
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

    private static void validarCoordenadas(Double latitud, Double longitud) {
        if ((latitud == null) != (longitud == null)) {
            throw new IllegalArgumentException("Debe indicar ambas coordenadas o ninguna");
        }
        if (latitud != null && (!Double.isFinite(latitud) || !Double.isFinite(longitud)
                || latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180)) {
            throw new IllegalArgumentException(
                    "Las coordenadas deben ser finitas y estar dentro de los rangos geográficos permitidos");
        }
    }
}
