package com.fulvo.operaciones.service;

import com.fulvo.operaciones.port.GeocodificacionPort;
import com.fulvo.operaciones.port.UbicacionResuelta;
import com.fulvo.operaciones.dto.CrearSedeRequest;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fulvo.common.exception.ConflictoEstadoException;

@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;
    private final GeocodificacionPort geocodificacion;

    public Sede crear(CrearSedeRequest request) {
        UbicacionResuelta ubicacion = geocodificacion.resolver(
                request.getDireccion(), request.getLatitudManual(), request.getLongitudManual());

        if (sedeRepository.existsByLatitudAndLongitud(ubicacion.latitud(), ubicacion.longitud())) {
            throw new ConflictoEstadoException(
                    "Ya existe una sede registrada en las coordenadas: " + ubicacion.latitud() + ", " + ubicacion.longitud()
            );
        }

        Sede sede = new Sede(
                request.getNombre(),
                request.getDireccion(),
                ubicacion.latitud(),
                ubicacion.longitud()
        );

        return sedeRepository.save(sede);
    }
}
