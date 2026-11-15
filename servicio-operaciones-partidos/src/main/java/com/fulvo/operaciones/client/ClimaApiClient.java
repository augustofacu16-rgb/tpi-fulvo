package com.fulvo.operaciones.client;

import com.fulvo.common.dto.ClimaPronosticoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Cliente HTTP basado en RestClient para consultar el pronóstico meteorológico
 * en servicio-clima (GET /v1/clima/pronosticos) (FN-08, RN-07).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClimaApiClient {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RestClient climaRestClient;

    /**
     * Consulta la evaluación climática para una ubicación y fecha/hora determinadas.
     *
     * @param latitud   Latitud de la sede.
     * @param longitud  Longitud de la sede.
     * @param fechaHora Fecha y hora programada del encuentro.
     * @return Pronóstico con el indicador de severidad {@code condicionAdversa}.
     */
    public ClimaPronosticoDTO consultarPronostico(Double latitud, Double longitud, LocalDateTime fechaHora) {
        String correlationId = MDC.get("correlationId");
        String fechaFormateada = fechaHora.truncatedTo(ChronoUnit.SECONDS).format(ISO_FORMATTER);

        log.debug("Consultando pronóstico en servicio-clima para ({}, {}) en fecha {}",
                latitud, longitud, fechaFormateada);

        return climaRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/clima/pronosticos")
                        .queryParam("latitud", latitud)
                        .queryParam("longitud", longitud)
                        .queryParam("fechaHora", fechaFormateada)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (correlationId != null) {
                        headers.set("X-Correlation-ID", correlationId);
                    }
                })
                .retrieve()
                .body(ClimaPronosticoDTO.class);
    }
}
