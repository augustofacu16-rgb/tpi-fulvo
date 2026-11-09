package com.fulvo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración programática de rutas de Spring Cloud Gateway mediante RouteLocatorBuilder (Apunte 19).
 * Desacopla la red externa del perímetro interno hacia servicio-torneos y servicio-operaciones-partidos.
 */
@Configuration
public class GatewayRoutesConfig {

    @Value("${services.torneos.url:http://localhost:8081}")
    private String torneosServiceUrl;

    @Value("${services.operaciones.url:http://localhost:8082}")
    private String operacionesServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Rutas hacia servicio-torneos (puerto interno 8081)
                .route("servicio-torneos-route", r -> r
                        .path("/v1/torneos/**", "/v1/solicitudes-inscripcion/**", "/v1/equipos/**")
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(torneosServiceUrl))
                // Rutas hacia servicio-operaciones-partidos (puerto interno 8082)
                .route("servicio-operaciones-route", r -> r
                        .path("/v1/partidos/**", "/v1/sedes/**", "/v1/arbitros/**", "/v1/tablas-posiciones/**")
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(operacionesServiceUrl))
                .build();
    }
}
