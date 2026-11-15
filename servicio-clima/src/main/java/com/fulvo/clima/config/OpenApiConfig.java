package com.fulvo.clima.config;

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
 * Configuración de OpenAPI 3.0 y Swagger UI para el adaptador de Evaluación Climática (Subsección 4.2 y 5.4).
 * Define metadatos base, esquemas de seguridad Bearer JWT y compatibilidad perimetral con API Gateway.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI climaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Evaluación Climática - TPI Fulvo")
                        .description("Microservicio adaptador encargado de la consulta de pronósticos meteorológicos " +
                                "y evaluación de severidad climática vía Open-Meteo para la programación y suspensión de partidos (FN-08, RN-07).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cátedra Backend de Aplicaciones - UTN FRC")
                                .url("https://frc.utn.edu.ar")))
                .servers(List.of(
                        new Server().url("/").description("Ruta relativa a través del API Gateway (Puerto 8080)"),
                        new Server().url("http://localhost:8085").description("Acceso directo local a Clima (Puerto 8085)")
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
