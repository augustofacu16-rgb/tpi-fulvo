package com.fulvo.operaciones.client;

import com.fulvo.common.dto.GeocodificacionDTO;
import com.fulvo.operaciones.exception.GeocodificacionIntegracionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeocodificacionApiClient {

    private final RestClient geocodificacionRestClient;

    public GeocodificacionDTO resolver(GeocodificacionDTO solicitud) {
        try {
            String correlationId = MDC.get("correlationId");

            GeocodificacionDTO resultado = geocodificacionRestClient.post()
                    .uri("/v1/geocodificaciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (correlationId != null) {
                            headers.set("X-Correlation-ID", correlationId);
                        }
                    })
                    .body(solicitud)
                    .retrieve()
                    .body(GeocodificacionDTO.class);

            if (resultado == null
                    || resultado.getLatitud() == null
                    || resultado.getLongitud() == null) {
                log.error("El servicio de geocodificación devolvió una respuesta sin coordenadas");
                throw new GeocodificacionIntegracionException(
                        "El servicio de geocodificación devolvió una respuesta incompleta",
                        HttpStatus.BAD_GATEWAY,
                        null
                );
            }

            return resultado;

        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            log.warn("El servicio de geocodificación respondió con status HTTP {}", statusCode);

            if (statusCode == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
                throw new GeocodificacionIntegracionException(
                        "No se encontraron coordenadas para la dirección indicada",
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        ex
                );
            }

            // Indisponibilidad del adaptador (503), error interno (500), etc. se traducen a 502 Bad Gateway
            throw new GeocodificacionIntegracionException(
                    "El servicio de geocodificación no está disponible actualmente",
                    HttpStatus.BAD_GATEWAY,
                    ex
            );

        } catch (ResourceAccessException ex) {
            log.warn("Fallo de conexión o timeout con el servicio de geocodificación: {}", ex.getMessage());
            throw new GeocodificacionIntegracionException(
                    "No se pudo conectar con el servicio de geocodificación",
                    HttpStatus.BAD_GATEWAY,
                    ex
            );
        }
    }
}