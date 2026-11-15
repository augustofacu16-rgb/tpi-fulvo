package com.fulvo.torneos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Configuración de clientes RestClient para comunicación inter-servicio en servicio-torneos.
 * Satisface la directiva arquitectónica: RestClient fluido con Spring 6 / Boot 3/4 y sin RestTemplate.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient notificacionesRestClient(
            @Value("${servicios.notificaciones.url:http://localhost:8083}") String notificacionesUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(notificacionesUrl)
                .build();
    }

    @Bean
    public RestClient operacionesRestClient(
            @Value("${servicios.operaciones.url:http://localhost:8082}") String operacionesUrl) {
        return RestClient.builder()
                .baseUrl(operacionesUrl)
                .build();
    }
}
