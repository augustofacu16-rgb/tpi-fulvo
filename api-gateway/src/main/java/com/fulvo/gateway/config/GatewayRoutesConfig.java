package com.fulvo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración programática de rutas de Spring Cloud Gateway mediante RouteLocatorBuilder (Apunte 19).
 * Desacopla la red externa del perímetro interno hacia servicio-torneos y servicio-operaciones-partidos,
 * e integra la agregación perimetral de especificaciones OpenAPI 3.0 (Subsección 2.6).
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
                // Rutas para agregación perimetral de OpenAPI 3.0 downstream (Subsección 2.6)
                .route("torneos-openapi-route", r -> r
                        .path("/api/torneos/v3/api-docs", "/api/torneos/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/api/torneos/(?<remaining>.*)", "/${remaining}")
                                       .setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(torneosServiceUrl))
                .route("operaciones-openapi-route", r -> r
                        .path("/api/partidos/v3/api-docs", "/api/partidos/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/api/partidos/(?<remaining>.*)", "/${remaining}")
                                       .setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(operacionesServiceUrl))

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
