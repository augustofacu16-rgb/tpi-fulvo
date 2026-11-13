package com.fulvo.geocodificacion.client;

import com.fulvo.geocodificacion.dto.NominatimResultado;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class NominatimClient {

    private final RestClient nominatimRestClient;

    public NominatimResultado[] buscar(String direccion) {
        String correlationId = MDC.get("correlationId");

        return nominatimRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", "{direccion}")
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build(direccion))
                .headers(headers -> {
                    if (correlationId != null) {
                        headers.set("X-Correlation-ID", correlationId);
                    }
                })
                .retrieve()
                .body(NominatimResultado[].class);
    }
}