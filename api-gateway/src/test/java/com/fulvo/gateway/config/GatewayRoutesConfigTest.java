package com.fulvo.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SetRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GatewayRoutesConfig en api-gateway - Tests unitarios de enrutamiento y agregación Swagger")
class GatewayRoutesConfigTest {

    @Test
    @DisplayName("Debe registrar las rutas perimetrales de OpenAPI agregadas hacia microservicios downstream")
    void debeRegistrarRutasOpenAPIAgregadas() {
        GatewayRoutesConfig config = new GatewayRoutesConfig();
        ReflectionTestUtils.setField(config, "torneosServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(config, "operacionesServiceUrl", "http://localhost:8082");

        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(WebFluxProperties.class);
        context.registerBean(PathRoutePredicateFactory.class);
        context.registerBean(RewritePathGatewayFilterFactory.class);
        context.registerBean(SetRequestHeaderGatewayFilterFactory.class);
        context.refresh();

        RouteLocatorBuilder builder = new RouteLocatorBuilder(context);

        RouteLocator routeLocator = config.customRouteLocator(builder);
        Flux<Route> routesFlux = routeLocator.getRoutes();
        List<Route> routes = routesFlux.collectList().block();

        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        boolean hasTorneosOpenApiRoute = routes.stream()
                .anyMatch(r -> "torneos-openapi-route".equals(r.getId()));
        boolean hasOperacionesOpenApiRoute = routes.stream()
                .anyMatch(r -> "operaciones-openapi-route".equals(r.getId()));
        boolean hasTorneosBusinessRoute = routes.stream()
                .anyMatch(r -> "servicio-torneos-route".equals(r.getId()));
        boolean hasOperacionesBusinessRoute = routes.stream()
                .anyMatch(r -> "servicio-operaciones-route".equals(r.getId()));

        assertTrue(hasTorneosOpenApiRoute, "Debe incluir la ruta torneos-openapi-route");
        assertTrue(hasOperacionesOpenApiRoute, "Debe incluir la ruta operaciones-openapi-route");
        assertTrue(hasTorneosBusinessRoute, "Debe incluir la ruta servicio-torneos-route");
        assertTrue(hasOperacionesBusinessRoute, "Debe incluir la ruta servicio-operaciones-route");
    }
}
