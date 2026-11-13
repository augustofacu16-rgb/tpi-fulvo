package com.fulvo.operaciones.service;

import com.fulvo.common.dto.GeocodificacionDTO;
import com.fulvo.operaciones.client.GeocodificacionApiClient;
import com.fulvo.operaciones.dto.CrearSedeRequest;
import com.fulvo.operaciones.models.Sede;
import com.fulvo.operaciones.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;
    private final GeocodificacionApiClient geocodificacionApiClient;

    public Sede crear(CrearSedeRequest request) {
        GeocodificacionDTO solicitud = GeocodificacionDTO.builder()
                .direccion(request.getDireccion())
                .latitud(request.getLatitudManual())
                .longitud(request.getLongitudManual())
                .build();

        GeocodificacionDTO ubicacion =
                geocodificacionApiClient.resolver(solicitud);

        Sede sede = new Sede(
                request.getNombre(),
                request.getDireccion(),
                ubicacion.getLatitud(),
                ubicacion.getLongitud()
        );

        return sedeRepository.save(sede);
    }
}