package com.fulvo.operaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente RestClient para consumir servicio-notificaciones.
 */
@Configuration
public class NotificacionesClientConfig {

    @Bean
    public RestClient notificacionesRestClient(
            RestClient.Builder builder,
            @Value("${notificaciones.base-url}") String baseUrl,
            @Value("${notificaciones.connect-timeout-ms}") int connectTimeout,
            @Value("${notificaciones.read-timeout-ms}") int readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
