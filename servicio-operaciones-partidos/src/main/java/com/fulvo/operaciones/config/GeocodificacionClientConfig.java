package com.fulvo.operaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GeocodificacionClientConfig {

    @Bean
    public RestClient geocodificacionRestClient(
            RestClient.Builder builder,
            @Value("${geocodificacion.base-url}") String baseUrl,
            @Value("${geocodificacion.connect-timeout-ms}") int connectTimeout,
            @Value("${geocodificacion.read-timeout-ms}") int readTimeout) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}