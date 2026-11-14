package com.fulvo.operaciones.client;

import com.fulvo.common.dto.DescalificarEquiposRequest;
import com.fulvo.operaciones.exception.TorneosIntegracionException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TorneosApiClient {

    private final RestClient restClient;

    public TorneosApiClient(@Qualifier("torneosRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void descalificarPorIncidentes(Long torneoId, Long localId, Long visitanteId) {
        try {
            restClient.post()
                    .uri("/internal/v1/torneos/{id}/descalificaciones-incidentes", torneoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) {
                            headers.set("X-Correlation-ID", correlationId);
                        }
                    })
                    .body(new DescalificarEquiposRequest(localId, visitanteId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new TorneosIntegracionException(
                    "No se pudo confirmar la descalificación de ambos equipos en Torneos", ex);
        }
    }
}
