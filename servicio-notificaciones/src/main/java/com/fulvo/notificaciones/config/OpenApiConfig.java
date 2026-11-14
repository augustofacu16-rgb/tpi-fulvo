package com.fulvo.notificaciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI 3.0 y Swagger UI para el adaptador de Notificaciones Transaccionales (Subsección 4.3).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificacionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Notificaciones Transaccionales - TPI Fulvo")
                        .description("Microservicio adaptador encargado del despacho de correos electrónicos transaccionales " +
                                "vía SMTP (Mailpit) para los eventos del ciclo de vida de torneos y partidos (FN-14, RN-11, RN-12).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cátedra Backend de Aplicaciones - UTN FRC")
                                .url("https://frc.utn.edu.ar")))
                .servers(List.of(
                        new Server().url("/").description("Ruta relativa a través del API Gateway (Puerto 8080)"),
                        new Server().url("http://localhost:8083").description("Acceso directo local a Notificaciones (Puerto 8083)")
                ));
    }
}
