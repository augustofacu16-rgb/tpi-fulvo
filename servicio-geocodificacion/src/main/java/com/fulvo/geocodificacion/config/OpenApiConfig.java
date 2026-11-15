package com.fulvo.geocodificacion.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI 3.0 y Swagger UI para el adaptador de Geocodificación Satelital (Subsección 4.1 y 5.4).
 * Define metadatos base, esquemas de seguridad Bearer JWT y compatibilidad perimetral con API Gateway.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI geocodificacionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Geocodificación Satelital - TPI Fulvo")
                        .description("Microservicio adaptador encargado de resolver coordenadas geográficas (latitud/longitud) " +
                                "y normalizar direcciones de sedes deportivas mediante Nominatim / OpenStreetMap con resiliencia (FN-02, RN-05).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cátedra Backend de Aplicaciones - UTN FRC")
                                .url("https://frc.utn.edu.ar")))
                .servers(List.of(
                        new Server().url("/").description("Ruta relativa a través del API Gateway (Puerto 8080)"),
                        new Server().url("http://localhost:8084").description("Acceso directo local a Geocodificación (Puerto 8084)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Autenticación OIDC vía Bearer Token JWT emitido por Keycloak (fulvo-realm).")));
    }
}
