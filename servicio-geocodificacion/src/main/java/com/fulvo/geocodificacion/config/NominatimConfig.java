package com.fulvo.geocodificacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class NominatimConfig {

    @Bean
    public RestClient nominatimRestClient(
            RestClient.Builder builder,
            @Value("${nominatim.base-url}") String baseUrl,
            @Value("${nominatim.connect-timeout-ms}") int connectTimeout,
            @Value("${nominatim.read-timeout-ms}") int readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .defaultHeader(
                        "User-Agent",
                        "Fulvo-TPI/1.0 (+https://github.com/augustofacu16-rgb/tpi-fulvo)"
                )
                .requestFactory(requestFactory)
                .build();
    }

}
