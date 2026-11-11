package com.fulvo.gateway.config;

import com.fulvo.gateway.security.KeycloakReactiveRoleConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakReactiveRoleConverter keycloakReactiveRoleConverter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Endpoints de salud, diagnóstico y Swagger UI / OpenAPI 3.0 (Subsección 2.6)
                .pathMatchers(
                    "/health-check", "/health-check/**", "/info",
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs", "/v3/api-docs/**",
                    "/api/torneos/v3/api-docs", "/api/torneos/v3/api-docs/**",
                    "/api/partidos/v3/api-docs", "/api/partidos/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()

                // Reglas perimetrales de Torneos (Subsecciones 1.5, 2.1, 2.2, 2.5)
                .pathMatchers(HttpMethod.POST, "/v1/torneos").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PATCH, "/v1/torneos/*/estado").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.POST, "/v1/torneos/*/fixtures").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PATCH, "/v1/solicitudes-inscripcion/*/admision").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.DELETE, "/v1/torneos/*/equipos/*").hasAnyAuthority("ROL_ORGANIZADOR", "ROL_DELEGADO", "ROLE_ORGANIZADOR", "ROLE_DELEGADO")
                .pathMatchers(HttpMethod.POST, "/v1/equipos/*/reemplazos-jugadores").hasAnyAuthority("ROL_DELEGADO", "ROLE_DELEGADO")

                // Reglas perimetrales de Operaciones (Subsecciones 1.5, 3.1, 3.2, 3.5)
                .pathMatchers(HttpMethod.POST, "/v1/sedes").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.POST, "/v1/arbitros").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/asignaciones").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/reprogramaciones").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PATCH, "/v1/partidos/*/estado").hasAnyAuthority("ROL_ARBITRO", "ROLE_ARBITRO")
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/actas").hasAnyAuthority("ROL_ARBITRO", "ROLE_ARBITRO")

                // Creación de solicitudes de inscripción: usuarios autenticados (ROL_FANATICO u organizador)
                .pathMatchers(HttpMethod.POST, "/v1/torneos/*/solicitudes-inscripcion").authenticated()

                // Consultas GET: cualquier usuario autenticado
                .pathMatchers(HttpMethod.GET, "/v1/**").authenticated()

                // Cualquier otra solicitud requiere autenticación
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakReactiveRoleConverter))
            );

        return http.build();
    }
}
