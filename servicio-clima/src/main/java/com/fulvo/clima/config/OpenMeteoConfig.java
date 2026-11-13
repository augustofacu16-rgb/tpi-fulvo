package com.fulvo.clima.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente HTTP RestClient para consumir Open-Meteo / WireMock.
 */
@Configuration
public class OpenMeteoConfig {

    @Bean
    public RestClient openMeteoRestClient(
            RestClient.Builder builder,
            @Value("${open-meteo.base-url}") String baseUrl,
            @Value("${open-meteo.connect-timeout-ms}") int connectTimeout,
            @Value("${open-meteo.read-timeout-ms}") int readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
