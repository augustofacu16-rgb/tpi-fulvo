package com.fulvo.operaciones.client;

import com.fulvo.common.dto.DescalificarEquiposRequest;
import com.fulvo.common.dto.JugadorPlantelDTO;
import com.fulvo.common.dto.ReprogramacionPartidoDTO;
import com.fulvo.common.dto.SuspensionPartidoDTO;
import com.fulvo.operaciones.exception.TorneosIntegracionException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cliente HTTP para la comunicación síncrona hacia servicio-torneos (Subsecciones 3.2 y 4.5).
 */
@Slf4j
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

    public Map<String, JugadorPlantelDTO> obtenerJugadoresTorneo(Long torneoId) {
        try {
            List<JugadorPlantelDTO> lista = restClient.get()
                    .uri("/v1/torneos/{id}/jugadores", torneoId)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) {
                            headers.set("X-Correlation-ID", correlationId);
                        }
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<JugadorPlantelDTO>>() {});

            if (lista == null) {
                return Map.of();
            }

            // Devuelve un mapa con clave "equipoId:dorsal" para búsqueda inmediata O(1)
            return lista.stream().collect(Collectors.toMap(
                    j -> j.equipoId() + ":" + j.dorsal(),
                    j -> j,
                    (existente, reemplazo) -> existente
            ));
        } catch (Exception ex) {
            return Map.of(); // Si Torneos no responde, no rompe la tabla de goleadores
        }
    }

    /**
     * Notifica la suspensión de un partido hacia servicio-torneos para mantener la consistencia del cronograma (Subsección 4.5, RN-07, RN-12).
     */
    public void notificarSuspensionPartido(Long torneoId, SuspensionPartidoDTO dto) {
        try {
            restClient.post()
                    .uri("/internal/v1/torneos/{id}/partidos/suspension", torneoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) {
                            headers.set("X-Correlation-ID", correlationId);
                        }
                    })
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Suspensión del partido #{} notificada con éxito a servicio-torneos", dto.partidoId());
        } catch (Exception ex) {
            log.warn("No se pudo notificar la suspensión del partido #{} a servicio-torneos: {}", dto.partidoId(), ex.getMessage());
        }
    }

    /**
     * Notifica la reprogramación de un partido hacia servicio-torneos para actualizar el calendario (Subsección 4.5).
     */
    public void notificarReprogramacionPartido(Long torneoId, ReprogramacionPartidoDTO dto) {
        try {
            restClient.post()
                    .uri("/internal/v1/torneos/{id}/partidos/reprogramacion", torneoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) {
                            headers.set("X-Correlation-ID", correlationId);
                        }
                    })
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Reprogramación del partido #{} notificada con éxito a servicio-torneos", dto.partidoId());
        } catch (Exception ex) {
            log.warn("No se pudo notificar la reprogramación del partido #{} a servicio-torneos: {}", dto.partidoId(), ex.getMessage());
        }
    }
}
