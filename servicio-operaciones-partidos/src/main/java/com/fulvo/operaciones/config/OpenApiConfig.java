package com.fulvo.operaciones.config;

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
 * Configuración de OpenAPI 3.0 y Swagger UI para el microservicio de Operaciones de Partidos (Subsección 2.6).
 * Expone la especificación viva del microservicio e integra el esquema de seguridad Bearer JWT (Keycloak).
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI operacionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Operaciones de Partidos - TPI Fulvo")
                        .description("Microservicio encargado de la administración de partidos, asignación anti-superposición de sedes y árbitros, " +
                                "recepción de lotes de fixture, cierre de actas de juego, definición de penales y cómputo de tablas de posiciones.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cátedra Backend de Aplicaciones - UTN FRC")
                                .url("https://frc.utn.edu.ar")))
                .servers(List.of(
                        new Server().url("/").description("Ruta relativa a través del API Gateway (Puerto 8080)"),
                        new Server().url("http://localhost:8082").description("Acceso directo local a Operaciones (Puerto 8082)")
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
