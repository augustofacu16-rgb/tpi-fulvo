package com.fulvo.operaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente RestClient para consumir servicio-clima.
 */
@Configuration
public class ClimaClientConfig {

    @Bean
    public RestClient climaRestClient(
            RestClient.Builder builder,
            @Value("${clima.base-url}") String baseUrl,
            @Value("${clima.connect-timeout-ms}") int connectTimeout,
            @Value("${clima.read-timeout-ms}") int readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
