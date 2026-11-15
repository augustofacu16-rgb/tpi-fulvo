package com.fulvo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración programática de rutas de Spring Cloud Gateway mediante RouteLocatorBuilder (Apunte 19).
 * Desacopla la red externa del perímetro interno hacia servicio-torneos, servicio-operaciones-partidos
 * y los microservicios adaptadores (servicio-geocodificacion, servicio-clima, servicio-notificaciones),
 * asegurando la precedencia de subrecursos operativos (Subsecciones 2.5, 3.5), el enrutamiento y aislamiento
 * de adaptadores (Subsección 4.6) y la agregación perimetral de especificaciones OpenAPI 3.0 (Subsección 2.6).
 * Responsable: Tomas Augusto Quiróz.
 */
@Configuration
public class GatewayRoutesConfig {

    @Value("${services.torneos.url:http://localhost:8081}")
    private String torneosServiceUrl;

    @Value("${services.operaciones.url:http://localhost:8082}")
    private String operacionesServiceUrl;

    @Value("${services.geocodificacion.url:http://localhost:8084}")
    private String geocodificacionServiceUrl;

    @Value("${services.clima.url:http://localhost:8085}")
    private String climaServiceUrl;

    @Value("${services.notificaciones.url:http://localhost:8083}")
    private String notificacionesServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 1. Rutas para agregación perimetral de OpenAPI 3.0 downstream (Subsección 2.6 y Etapa 5)
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
                .route("notificaciones-openapi-route", r -> r
                        .path("/api/notificaciones/v3/api-docs", "/api/notificaciones/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/api/notificaciones/(?<remaining>.*)", "/${remaining}")
                                .setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(notificacionesServiceUrl))
                .route("geocodificacion-openapi-route", r -> r
                        .path("/api/geocodificaciones/v3/api-docs", "/api/geocodificaciones/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/api/geocodificaciones/(?<remaining>.*)", "/${remaining}")
                                .setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(geocodificacionServiceUrl))
                .route("clima-openapi-route", r -> r
                        .path("/api/clima/v3/api-docs", "/api/clima/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/api/clima/(?<remaining>.*)", "/${remaining}")
                                .setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(climaServiceUrl))
                // 2. Precedencia de subrecursos de torneos delegados a operaciones (Subsecciones 2.5 y 3.5)
                .route("servicio-operaciones-torneos-subrecursos-route", r -> r
                        .path(
                                "/v1/torneos/*/partidos", "/v1/torneos/*/partidos/**",
                                "/v1/torneos/*/posiciones", "/v1/torneos/*/posiciones/**",
                                "/v1/torneos/*/goleadores", "/v1/torneos/*/goleadores/**"
                        )
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(operacionesServiceUrl))
                // 3. Rutas principales hacia servicio-torneos (puerto interno 8081)
                .route("servicio-torneos-route", r -> r
                        .path(
                                "/v1/torneos", "/v1/torneos/**",
                                "/v1/solicitudes-inscripcion", "/v1/solicitudes-inscripcion/**",
                                "/v1/equipos", "/v1/equipos/**"
                        )
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(torneosServiceUrl))
                // 4. Rutas principales hacia servicio-operaciones-partidos (puerto interno 8082 - Subsección 3.5)
                .route("servicio-operaciones-route", r -> r
                        .path(
                                "/v1/partidos", "/v1/partidos/**",
                                "/v1/sedes", "/v1/sedes/**",
                                "/v1/arbitros", "/v1/arbitros/**",
                                "/v1/tablas-posiciones", "/v1/tablas-posiciones/**"
                        )
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(operacionesServiceUrl))
                // 5. Rutas hacia microservicios adaptadores externos (Subsección 4.6)
                .route("servicio-geocodificacion-route", r -> r
                        .path("/v1/geocodificaciones", "/v1/geocodificaciones/**")
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(geocodificacionServiceUrl))
                .route("servicio-clima-route", r -> r
                        .path("/v1/clima", "/v1/clima/**")
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(climaServiceUrl))
                .route("servicio-notificaciones-route", r -> r
                        .path("/v1/notificaciones", "/v1/notificaciones/**")
                        .filters(f -> f.setRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
                        .uri(notificacionesServiceUrl))
                .build();
    }
}
