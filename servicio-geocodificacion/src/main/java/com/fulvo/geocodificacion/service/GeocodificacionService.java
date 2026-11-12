package com.fulvo.geocodificacion.service;

import com.fulvo.common.dto.GeocodificacionDTO;
import com.fulvo.geocodificacion.client.NominatimClient;
import com.fulvo.geocodificacion.dto.NominatimResultado;
import com.fulvo.geocodificacion.exception.DireccionNoEncontradaException;
import com.fulvo.geocodificacion.exception.GeocodificacionNoDisponibleException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodificacionService {

    private final NominatimClient nominatimClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public GeocodificacionDTO resolver(GeocodificacionDTO solicitud) {
        Double latitudManual = solicitud.getLatitud();
        Double longitudManual = solicitud.getLongitud();

        if ((latitudManual == null) != (longitudManual == null)) {
            throw new IllegalArgumentException(
                    "Debe indicar ambas coordenadas manuales o ninguna"
            );
        }

        if (latitudManual != null) {
            validarCoordenadas(latitudManual, longitudManual);
        }

        try {
            return circuitBreakerRegistry.circuitBreaker("nominatim")
                    .executeSupplier(() -> consultarProveedor(
                            solicitud.getDireccion()
                    ));
        } catch (DireccionNoEncontradaException exception) {
            if (latitudManual != null && longitudManual != null) {
                return new GeocodificacionDTO(
                        solicitud.getDireccion(),
                        latitudManual,
                        longitudManual,
                        "MANUAL",
                        null
                );
            }

            throw exception;

        } catch (RestClientException
                 | CallNotPermittedException
                 | IllegalArgumentException
                 | IllegalStateException exception) {
            if (latitudManual != null && longitudManual != null) {
                log.warn("Proveedor no disponible; se usan coordenadas manuales",
                        exception);

                return new GeocodificacionDTO(
                        solicitud.getDireccion(),
                        latitudManual,
                        longitudManual,
                        "MANUAL",
                        null
                );


            }
            throw new GeocodificacionNoDisponibleException(
                    "El servicio de geocodificación no está disponible",
                    exception
            );
        }


    }


    private GeocodificacionDTO consultarProveedor(String direccion) {
        NominatimResultado[] resultados = nominatimClient.buscar(direccion);

        if (resultados == null || resultados.length == 0
                || resultados[0] == null) {
            throw new DireccionNoEncontradaException(
                    "No se encontraron resultados para la dirección indicada"
            );
        }

        NominatimResultado resultado = resultados[0];

        if (resultado.lat() == null || resultado.lon() == null) {
            throw new IllegalStateException(
                    "El proveedor devolvió coordenadas incompletas"
            );
        }

        double latitud = Double.parseDouble(resultado.lat());
        double longitud = Double.parseDouble(resultado.lon());

        return new GeocodificacionDTO(
                direccion,
                latitud,
                longitud,
                "NOMINATIM",
                resultado.displayName()
        );

    }

    private void validarCoordenadas(double latitud, double longitud) {
        if (!Double.isFinite(latitud) || !Double.isFinite(longitud)
                || latitud < -90 || latitud > 90
                || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException(
                    "Las coordenadas deben ser finitas y estar dentro "
                            + "de los rangos geográficos permitidos"
            );
        }
    }
}
