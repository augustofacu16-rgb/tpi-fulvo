package com.fulvo.operaciones.dto;

public record SedeResponse(
        Long id,
        String nombre,
        String direccion,
        Double latitud,
        Double longitud
) {
}