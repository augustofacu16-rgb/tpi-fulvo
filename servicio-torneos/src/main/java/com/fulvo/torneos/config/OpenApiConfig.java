package com.fulvo.torneos.config;

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
 * Configuración de OpenAPI 3.0 y Swagger UI para el microservicio de Torneos (Subsección 2.6).
 * Define metadatos base del servicio, esquemas de seguridad Bearer JWT y compatibilidad perimetral con Gateway.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI torneosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Gestión de Torneos - TPI Fulvo")
                        .description("Microservicio encargado de la administración de torneos, solicitudes de inscripción de equipos, " +
                                "control estricto de cupos, validación de planteles de buena fe y generación algorítmica determinística de fixtures.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cátedra Backend de Aplicaciones - UTN FRC")
                                .url("https://frc.utn.edu.ar")))
                .servers(List.of(
                        new Server().url("/").description("Ruta relativa a través del API Gateway (Puerto 8080)"),
                        new Server().url("http://localhost:8081").description("Acceso directo local a Torneos (Puerto 8081)")
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
