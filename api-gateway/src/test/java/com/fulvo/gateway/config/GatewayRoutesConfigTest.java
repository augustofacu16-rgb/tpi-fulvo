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
    @DisplayName("Debe registrar las rutas perimetrales de OpenAPI agregadas, negocio y adaptadores hacia microservicios downstream")
    void debeRegistrarRutasOpenAPIAgregadas() {
        GatewayRoutesConfig config = new GatewayRoutesConfig();
        ReflectionTestUtils.setField(config, "torneosServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(config, "operacionesServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(config, "geocodificacionServiceUrl", "http://localhost:8084");
        ReflectionTestUtils.setField(config, "climaServiceUrl", "http://localhost:8085");
        ReflectionTestUtils.setField(config, "notificacionesServiceUrl", "http://localhost:8083");

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
        boolean hasNotificacionesOpenApiRoute = routes.stream()
                .anyMatch(r -> "notificaciones-openapi-route".equals(r.getId()));
        boolean hasGeocodificacionOpenApiRoute = routes.stream()
                .anyMatch(r -> "geocodificacion-openapi-route".equals(r.getId()));
        boolean hasClimaOpenApiRoute = routes.stream()
                .anyMatch(r -> "clima-openapi-route".equals(r.getId()));
        boolean hasSubrecursosOperacionesRoute = routes.stream()
                .anyMatch(r -> "servicio-operaciones-torneos-subrecursos-route".equals(r.getId()));
        boolean hasTorneosBusinessRoute = routes.stream()
                .anyMatch(r -> "servicio-torneos-route".equals(r.getId()));
        boolean hasOperacionesBusinessRoute = routes.stream()
                .anyMatch(r -> "servicio-operaciones-route".equals(r.getId()));
        boolean hasGeocodificacionRoute = routes.stream()
                .anyMatch(r -> "servicio-geocodificacion-route".equals(r.getId()));
        boolean hasClimaRoute = routes.stream()
                .anyMatch(r -> "servicio-clima-route".equals(r.getId()));
        boolean hasNotificacionesRoute = routes.stream()
                .anyMatch(r -> "servicio-notificaciones-route".equals(r.getId()));

        assertTrue(hasTorneosOpenApiRoute, "Debe incluir la ruta torneos-openapi-route");
        assertTrue(hasOperacionesOpenApiRoute, "Debe incluir la ruta operaciones-openapi-route");
        assertTrue(hasNotificacionesOpenApiRoute, "Debe incluir la ruta notificaciones-openapi-route");
        assertTrue(hasGeocodificacionOpenApiRoute, "Debe incluir la ruta geocodificacion-openapi-route");
        assertTrue(hasClimaOpenApiRoute, "Debe incluir la ruta clima-openapi-route");
        assertTrue(hasSubrecursosOperacionesRoute, "Debe incluir la ruta con precedencia de subrecursos a operaciones");
        assertTrue(hasTorneosBusinessRoute, "Debe incluir la ruta servicio-torneos-route");
        assertTrue(hasOperacionesBusinessRoute, "Debe incluir la ruta servicio-operaciones-route para sedes, arbitros y partidos");
        assertTrue(hasGeocodificacionRoute, "Debe incluir la ruta servicio-geocodificacion-route hacia adaptador de geocodificación");
        assertTrue(hasClimaRoute, "Debe incluir la ruta servicio-clima-route hacia adaptador de clima");
        assertTrue(hasNotificacionesRoute, "Debe incluir la ruta servicio-notificaciones-route hacia adaptador de notificaciones");

        // Validar URIs asignadas
        Route operacionesRoute = routes.stream()
                .filter(r -> "servicio-operaciones-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8082", operacionesRoute.getUri().toString());

        Route subrecursosRoute = routes.stream()
                .filter(r -> "servicio-operaciones-torneos-subrecursos-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8082", subrecursosRoute.getUri().toString());

        Route torneosRoute = routes.stream()
                .filter(r -> "servicio-torneos-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8081", torneosRoute.getUri().toString());

        Route geoRoute = routes.stream()
                .filter(r -> "servicio-geocodificacion-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8084", geoRoute.getUri().toString());

        Route climaRoute = routes.stream()
                .filter(r -> "servicio-clima-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8085", climaRoute.getUri().toString());

        Route notifRoute = routes.stream()
                .filter(r -> "servicio-notificaciones-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8083", notifRoute.getUri().toString());

        Route notifOpenApiRoute = routes.stream()
                .filter(r -> "notificaciones-openapi-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8083", notifOpenApiRoute.getUri().toString());

        Route geoOpenApiRoute = routes.stream()
                .filter(r -> "geocodificacion-openapi-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8084", geoOpenApiRoute.getUri().toString());

        Route climaOpenApiRoute = routes.stream()
                .filter(r -> "clima-openapi-route".equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("http://localhost:8085", climaOpenApiRoute.getUri().toString());
    }
}
