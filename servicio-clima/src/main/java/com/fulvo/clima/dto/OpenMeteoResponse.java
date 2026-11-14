package com.fulvo.clima.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO interno para deserializar la respuesta JSON de la API Open-Meteo (/v1/forecast).
 */
@Data
public class OpenMeteoResponse {

    @JsonProperty("current_weather")
    private CurrentWeather currentWeather;

    private Hourly hourly;

    @Data
    public static class CurrentWeather {
        private Double temperature;
        private Integer weathercode;
        private String time;
    }

    @Data
    public static class Hourly {
        private List<String> time;
        private List<Double> precipitation;
        private List<Integer> weathercode;
    }
}
