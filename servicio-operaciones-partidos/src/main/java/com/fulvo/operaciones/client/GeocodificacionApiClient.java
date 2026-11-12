package com.fulvo.operaciones.client;

import com.fulvo.common.dto.GeocodificacionDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.fulvo.operaciones.exception.GeocodificacionIntegracionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
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
                throw new GeocodificacionIntegracionException(
                        "Geocodificación devolvió una respuesta sin coordenadas",
                        HttpStatus.BAD_GATEWAY,
                        null
                );
            }

            return resultado;

        } catch (RestClientResponseException ex) {
            HttpStatus status = ex.getStatusCode().value() == 422
                    ? HttpStatus.UNPROCESSABLE_ENTITY
                    : HttpStatus.BAD_GATEWAY;

            throw new GeocodificacionIntegracionException(
                    "El servicio de geocodificación rechazó la solicitud",
                    status,
                    ex
            );

        } catch (ResourceAccessException ex) {
            throw new GeocodificacionIntegracionException(
                    "No se pudo conectar con el servicio de geocodificación",
                    HttpStatus.BAD_GATEWAY,
                    ex
            );
        }
    }
}