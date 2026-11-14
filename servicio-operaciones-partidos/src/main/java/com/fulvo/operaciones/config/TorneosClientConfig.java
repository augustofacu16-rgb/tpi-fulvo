package com.fulvo.operaciones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TorneosClientConfig {

    @Bean
    public RestClient torneosRestClient(
            RestClient.Builder builder,
            @Value("${torneos.base-url:http://localhost:8081}") String baseUrl,
            @Value("${torneos.connect-timeout-ms:2000}") int connectTimeout,
            @Value("${torneos.read-timeout-ms:5000}") int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }
}
