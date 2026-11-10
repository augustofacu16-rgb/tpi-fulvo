package com.fulvo.torneos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente RestClient para comunicación inter-servicio en servicio-torneos.
 * Satisface la directiva arquitectónica: RestClient fluido con Spring 6 / Boot 3 y sin RestTemplate.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient notificacionesRestClient(
            @Value("${servicios.notificaciones.url:http://localhost:8083}") String notificacionesUrl,
            RestClient.Builder builder) {
        return builder
                .baseUrl(notificacionesUrl)
                .build();
    }
}
