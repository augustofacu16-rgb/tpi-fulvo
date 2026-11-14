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
 * Implementa inspección JWT sin estado contra Keycloak y control de acceso basado en roles (RBAC),
 * garantizando reglas para Torneos (Subsección 2.5) y Operaciones/Arbitraje (Subsección 3.5),
 * devolviendo respuestas estandarizadas bajo RFC 7807 (ProblemDetail).
 * Responsable: Tomas Augusto Quiróz.
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

                // =========================================================================
                // 1. REGLAS PERIMETRALES DE TORNEOS (Subsección 2.5, FN-01 a FN-06, RN-01 a RN-04)
                // =========================================================================
                // Alta de certámenes (FN-01, RN-01): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/torneos", "/v1/torneos/")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Modificación de estado / Cierre de inscripciones (FN-06, RN-01): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.PATCH, "/v1/torneos/*/estado", "/v1/torneos/*/estado/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Generación determinística de fixture (FN-06): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/torneos/*/fixtures", "/v1/torneos/*/fixtures/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Postulación de equipo con lista de buena fe (FN-04, RN-02, RN-03): Exclusivo ROL_FANATICO
                .pathMatchers(HttpMethod.POST, "/v1/torneos/*/solicitudes-inscripcion", "/v1/torneos/*/solicitudes-inscripcion/**")
                    .hasAnyAuthority("ROL_FANATICO", "ROLE_FANATICO")

                // Admisión o rechazo de solicitudes de inscripción (FN-05, RN-04): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.PATCH, "/v1/solicitudes-inscripcion/*/admision", "/v1/solicitudes-inscripcion/*/admision/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Auditoría y consulta organizativa de solicitudes de inscripción: Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.GET, "/v1/torneos/*/solicitudes-inscripcion", "/v1/torneos/*/solicitudes-inscripcion/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Baja voluntaria o descalificación de equipo: ROL_ORGANIZADOR o ROL_DELEGADO (FN-13)
                .pathMatchers(HttpMethod.DELETE, "/v1/torneos/*/equipos/*", "/v1/torneos/*/equipos/*/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROL_DELEGADO", "ROLE_ORGANIZADOR", "ROLE_DELEGADO")

                // Reemplazo de jugadores en lista de buena fe (FN-11, RN-11, Subsección 3.4): Exclusivo ROL_DELEGADO
                .pathMatchers(HttpMethod.POST, "/v1/equipos/*/reemplazos-jugadores", "/v1/equipos/*/reemplazos-jugadores/**")
                    .hasAnyAuthority("ROL_DELEGADO", "ROLE_DELEGADO")

                // =========================================================================
                // 2. REGLAS PERIMETRALES DE OPERACIONES Y ARBITRAJE (Subsección 3.5, FN-02, FN-03, FN-07 a FN-10, RN-05 a RN-10)
                // =========================================================================
                // Alta, modificación y baja de Sedes deportivas (FN-02, RN-05): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/sedes", "/v1/sedes/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PUT, "/v1/sedes/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.DELETE, "/v1/sedes/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Empadronamiento, modificación y baja de Árbitros (FN-03, RN-05): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/arbitros", "/v1/arbitros/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PUT, "/v1/arbitros/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.DELETE, "/v1/arbitros/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Recepción de fixture en lote (Subsección 2.3): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/partidos/lote", "/v1/partidos/lote/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Asignación de sede, árbitro y fecha/hora anti-superposición (FN-07, RN-05): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/asignaciones", "/v1/partidos/*/asignaciones/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Reprogramación de partidos por contingencia climática (FN-08, RN-07, Subsección 4.4): Exclusivo ROL_ORGANIZADOR
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/reprogramaciones", "/v1/partidos/*/reprogramaciones/**")
                    .hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Transición de estado de partido (EN_CURSO, SUSPENDIDO, CANCELADO) (FN-09, RN-08, RN-09): Exclusivo ROL_ARBITRO
                .pathMatchers(
                        HttpMethod.PATCH,
                        "/v1/partidos/*/estado", "/v1/partidos/*/estado/**",
                        "/v1/partidos/*/cancelacion", "/v1/partidos/*/cancelacion/**"
                ).hasAnyAuthority("ROL_ARBITRO", "ROLE_ARBITRO")

                // Carga y asentamiento de actas oficiales con dorsales y penales (FN-10, RN-08, RN-10): Exclusivo ROL_ARBITRO
                .pathMatchers(HttpMethod.POST, "/v1/partidos/*/actas", "/v1/partidos/*/actas/**")
                    .hasAnyAuthority("ROL_ARBITRO", "ROLE_ARBITRO")

                // =========================================================================
                // 3. CONSULTAS GET PÚBLICAS / AUTENTICADAS (FN-12, Posiciones, Goleadores, Fixtures, Estadísticas)
                // =========================================================================
                // Cualquier usuario autenticado (ROL_FANATICO, ROL_DELEGADO, ROL_ARBITRO, ROL_ORGANIZADOR)
                // puede consultar partidos, tablas de posiciones, goleadores, sedes, árbitros y estadísticas.
                // Peticiones sin token son rechazadas con 401 Unauthorized por el GatewayAuthenticationEntryPoint.
                .pathMatchers(HttpMethod.GET, "/v1/**").authenticated()

                // Cualquier otra solicitud de mutación requiere privilegios administrativos
                .pathMatchers(HttpMethod.POST, "/v1/**").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PUT, "/v1/**").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.PATCH, "/v1/**").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")
                .pathMatchers(HttpMethod.DELETE, "/v1/**").hasAnyAuthority("ROL_ORGANIZADOR", "ROLE_ORGANIZADOR")

                // Bloqueo general por defecto
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
