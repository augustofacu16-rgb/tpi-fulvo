package com.fulvo.gateway.config;

import com.fulvo.gateway.security.GatewayAccessDeniedHandler;
import com.fulvo.gateway.security.GatewayAuthenticationEntryPoint;
import com.fulvo.gateway.security.KeycloakReactiveRoleConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad perimetral reactiva en API Gateway (Apuntes 20 y 21).
 * Implementa inspección JWT sin estado contra Keycloak y control de acceso basado en roles (RBAC)
 * devolviendo respuestas estandarizadas bajo RFC 7807 (ProblemDetail).
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakReactiveRoleConverter keycloakReactiveRoleConverter;
    private final GatewayAuthenticationEntryPoint gatewayAuthenticationEntryPoint;
    private final GatewayAccessDeniedHandler gatewayAccessDeniedHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(gatewayAuthenticationEntryPoint)
                .accessDeniedHandler(gatewayAccessDeniedHandler)
            )
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

                // Reglas perimetrales de Torneos (Subsección 2.5, FN-01 a FN-06, RN-01 a RN-04)
                // 1. Alta de certámenes (FN-01, RN-01): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/torneos").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // 2. Modificación de estado / Cierre de inscripciones (FN-06, RN-01): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.PATCH, "/v1/torneos/*/estado").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // 3. Generación determinística de fixture (FN-06): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/torneos/*/fixtures").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // 4. Postulación de equipo con lista de buena fe (FN-04, RN-02, RN-03): Exclusivo ROL_FANATICO
                .pathMatchers(HttpMethod.POST, "/v1/torneos/*/solicitudes-inscripcion").hasAnyAuthority("ROL_FANATICO", "ROLE_FANATICO")

                // 5. Admisión o rechazo de solicitudes de inscripción (FN-05, RN-04): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.PATCH, "/v1/solicitudes-inscripcion/*/admision").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // 6. Auditoría y consulta de solicitudes de inscripción: Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.GET, "/v1/torneos/*/solicitudes-inscripcion", "/v1/torneos/*/solicitudes-inscripcion/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // 7. Baja voluntaria o descalificación de equipo: ROL_ORGANIZADOR o ROL_DELEGADO
                .pathMatchers(HttpMethod.DELETE, "/v1/torneos/*/equipos/*")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROL_DELEGADO", "ROLE_ORGANIZADOR", "ROLE_DELEGADO")

                // 8. Reemplazo de jugadores en lista de buena fe (RN-11): Exclusivo ROL_DELEGADO
                .pathMatchers(HttpMethod.POST, "/v1/equipos/*/reemplazos-jugadores")
                    .hasAnyAuthority("ROL_DELEGADO", "ROLE_DELEGADO")

                // Reglas perimetrales de Operaciones (Subsecciones 3.1, 3.2, 3.5)
                .pathMatchers(HttpMethod.POST, "/v1/sedes").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.POST, "/v1/arbitros").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/asignaciones").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/reprogramaciones").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PATCH, "/v1/partidos/*/estado").hasAnyAuthority("ROL_ARBITRO", "ROLE_ARBITRO")
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/actas").hasAnyAuthority("ROL_ARBITRO", "ROLE_ARBITRO")

                // Consultas GET: cualquier usuario autenticado (torneos, equipos, fixture, posiciones, estadísticas)
                .pathMatchers(HttpMethod.GET, "/v1/**").authenticated()

                // Cualquier otra solicitud requiere autenticación
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakReactiveRoleConverter))
                .authenticationEntryPoint(gatewayAuthenticationEntryPoint)
                .accessDeniedHandler(gatewayAccessDeniedHandler)
            );

        return http.build();
    }
}
