package com.fulvo.operaciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenApiConfig en servicio-operaciones-partidos - Tests unitarios")
class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    @DisplayName("Debe inicializar OpenAPI con metadatos descriptivos y título de Operaciones")
    void debeInicializarOpenAPIConMetadatos() {
        OpenAPI openAPI = openApiConfig.operacionesOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Servicio de Operaciones de Partidos - TPI Fulvo", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertTrue(openAPI.getInfo().getDescription().contains("partidos"));
    }

    @Test
    @DisplayName("Debe registrar esquema de seguridad Bearer JWT (Keycloak) en Components")
    void debeRegistrarEsquemaDeSeguridadBearerJWT() {
        OpenAPI openAPI = openApiConfig.operacionesOpenAPI();

        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey(OpenApiConfig.SECURITY_SCHEME_NAME));

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }

    @Test
    @DisplayName("Debe configurar requisitos de seguridad globales y servidores perimetrales")
    void debeConfigurarRequisitosDeSeguridadYServidores() {
        OpenAPI openAPI = openApiConfig.operacionesOpenAPI();

        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().get(0).containsKey(OpenApiConfig.SECURITY_SCHEME_NAME));

        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        assertEquals("/", openAPI.getServers().get(0).getUrl());
    }
}
