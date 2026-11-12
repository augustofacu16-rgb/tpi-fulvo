package com.fulvo.torneos.client;

import com.fulvo.common.dto.CrucePartidoDTO;
import com.fulvo.common.exception.BusinessRuleException;
import com.fulvo.common.filter.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP para comunicación síncrona con servicio-operaciones-partidos (POST /v1/partidos/lote) (Subsección 2.2.3).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperacionesApiClient {

    private final RestClient operacionesRestClient;

    /**
     * Despacha el lote completo de cruces de fixture a servicio-operaciones-partidos,
     * propagando la cabecera X-Correlation-ID desde el contexto MDC de SLF4J (Apunte 23).
     *
     * @param cruces Lista de CrucePartidoDTO a persistir en esquema_operaciones.
     */
    public void despacharLotePartidos(List<CrucePartidoDTO> cruces) {
        log.info("Despachando lote de {} partidos a servicio-operaciones-partidos (POST /v1/partidos/lote)", cruces.size());
        try {
            String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

            ResponseEntity<Map<String, Object>> response = operacionesRestClient.post()
                    .uri("/v1/partidos/lote")
                    .headers(headers -> {
                        if (StringUtils.hasText(correlationId)) {
                            headers.set(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
                        }
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cruces)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

            log.info("Lote de partidos despachado exitosamente. Status: {}, Respuesta: {}",
                    response.getStatusCode(), response.getBody());
        } catch (Exception e) {
            log.error("Fallo crítico al despachar lote de partidos a servicio-operaciones-partidos: {}", e.getMessage(), e);
            throw new BusinessRuleException("Error al despachar el lote de partidos hacia el servicio de operaciones: " + e.getMessage());
        }
    }
}
