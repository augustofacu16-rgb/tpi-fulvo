package com.fulvo.clima.service;

import com.fulvo.clima.client.OpenMeteoClient;
import com.fulvo.clima.dto.OpenMeteoResponse;
import com.fulvo.common.dto.ClimaPronosticoDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Servicio encargado de consultar el pronóstico y evaluar severidad climática según RN-07.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClimaService {

    private static final double UMBRAL_PRECIPITACION_MM = 40.0;
    private static final Set<Integer> CODIGOS_TORMENTA_GRANIZO = Set.of(95, 96, 99);
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final OpenMeteoClient openMeteoClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ClimaPronosticoDTO evaluarPronostico(Double latitud, Double longitud, LocalDateTime fechaHora) {
        try {
            return circuitBreakerRegistry.circuitBreaker("openMeteo")
                    .executeSupplier(() -> consultarYEvaluar(latitud, longitud, fechaHora));
        } catch (RestClientException | CallNotPermittedException ex) {
            log.warn("Fallo al consultar proveedor climático (Circuit Breaker / red): {}", ex.getMessage());
            return construirFallback(
                    latitud,
                    longitud,
                    fechaHora,
                    "Pronóstico externo no disponible temporalmente (fallback preventivo)");
        }
    }

    private ClimaPronosticoDTO consultarYEvaluar(Double latitud, Double longitud, LocalDateTime fechaHora) {
        OpenMeteoResponse response = openMeteoClient.consultarPronostico(latitud, longitud);
        if (response == null) {
            return construirFallback(latitud, longitud, fechaHora, "Respuesta vacía del proveedor climático");
        }

        Double temperatura = response.getCurrentWeather() != null
                ? response.getCurrentWeather().getTemperature()
                : null;
        Double precipitacionMm = 0.0;
        Integer codigoClima = response.getCurrentWeather() != null && response.getCurrentWeather().getWeathercode() != null
                ? response.getCurrentWeather().getWeathercode()
                : 0;

        OpenMeteoResponse.Hourly hourly = response.getHourly();
        if (hourly != null && hourly.getTime() != null && !hourly.getTime().isEmpty()) {
            int idx = resolverIndiceHorario(hourly.getTime(), fechaHora);
            if (hourly.getPrecipitation() != null && idx < hourly.getPrecipitation().size()
                    && hourly.getPrecipitation().get(idx) != null) {
                precipitacionMm = hourly.getPrecipitation().get(idx);
            }
            if (hourly.getWeathercode() != null && idx < hourly.getWeathercode().size()
                    && hourly.getWeathercode().get(idx) != null) {
                codigoClima = hourly.getWeathercode().get(idx);
            }
        }

        boolean adversa = precipitacionMm > UMBRAL_PRECIPITACION_MM
                || CODIGOS_TORMENTA_GRANIZO.contains(codigoClima);

        String descripcion = adversa
                ? "Condición climática adversa: tormenta severa o precipitación superior a 40 mm"
                : "Condición climática apta para el encuentro";

        return ClimaPronosticoDTO.builder()
                .latitud(latitud)
                .longitud(longitud)
                .fechaHora(fechaHora)
                .temperatura(temperatura)
                .precipitacionMm(precipitacionMm)
                .codigoClima(codigoClima)
                .condicionAdversa(adversa)
                .descripcion(descripcion)
                .build();
    }

    private int resolverIndiceHorario(List<String> tiempos, LocalDateTime fechaHora) {
        String horaBuscada = fechaHora.truncatedTo(ChronoUnit.HOURS).format(HORA_FORMATTER);
        int exacto = tiempos.indexOf(horaBuscada);
        return exacto >= 0 ? exacto : 0;
    }

    private ClimaPronosticoDTO construirFallback(
            Double latitud,
            Double longitud,
            LocalDateTime fechaHora,
            String descripcion) {
        return ClimaPronosticoDTO.builder()
                .latitud(latitud)
                .longitud(longitud)
                .fechaHora(fechaHora)
                .precipitacionMm(0.0)
                .codigoClima(0)
                .condicionAdversa(false)
                .descripcion(descripcion)
                .build();
    }
}
