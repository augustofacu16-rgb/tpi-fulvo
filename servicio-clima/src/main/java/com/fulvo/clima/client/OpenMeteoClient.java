package com.fulvo.clima.client;

import com.fulvo.clima.dto.OpenMeteoResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP dedicado para consultar el pronóstico meteorológico en Open-Meteo o WireMock.
 */
@Component
@RequiredArgsConstructor
public class OpenMeteoClient {

    private final RestClient openMeteoRestClient;

    public OpenMeteoResponse consultarPronostico(Double latitud, Double longitud) {
        String correlationId = MDC.get("correlationId");

        return openMeteoRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitud)
                        .queryParam("longitude", longitud)
                        .queryParam("hourly", "precipitation,weathercode")
                        .queryParam("current_weather", true)
                        .queryParam("timezone", "auto")
                        .build())
                .headers(headers -> {
                    if (correlationId != null) {
                        headers.set("X-Correlation-ID", correlationId);
                    }
                })
                .retrieve()
                .body(OpenMeteoResponse.class);
    }
}
