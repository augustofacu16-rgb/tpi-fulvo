package com.fulvo.gateway.security;

import com.fulvo.gateway.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityConfigSwaggerAndRfc7807Test - Verificación unitaria reactiva de rutas OpenAPI públicas y RFC 7807")
class SecurityConfigSwaggerAndRfc7807Test {

    private final KeycloakReactiveRoleConverter roleConverter = new KeycloakReactiveRoleConverter();
    private final GatewayAuthenticationEntryPoint entryPoint = new GatewayAuthenticationEntryPoint();
    private final GatewayAccessDeniedHandler accessDeniedHandler = new GatewayAccessDeniedHandler();
    private final SecurityConfig securityConfig = new SecurityConfig(roleConverter, entryPoint, accessDeniedHandler);

    @Test
    @DisplayName("Debe verificar que las 7 rutas de Swagger UI y OpenAPI agregadas están configuradas como permitAll en SecurityConfig")
    void debeVerificarRutasSwaggerYOpenApiPermitAll() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        ReactiveJwtDecoder decoder = token -> Mono.empty();
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(decoder)));

        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
        assertNotNull(chain);

        List<String> permitAllPaths = List.of(
                "/swagger-ui.html",
                "/swagger-ui/index.html",
                "/v3/api-docs",
                "/v3/api-docs/swagger-config",
                "/api/torneos/v3/api-docs",
                "/api/partidos/v3/api-docs",
                "/api/notificaciones/v3/api-docs",
                "/api/geocodificaciones/v3/api-docs",
                "/api/clima/v3/api-docs"
        );

        WebFilterChainProxy proxy = new WebFilterChainProxy(chain);
        WebTestClient client = WebTestClient.bindToWebHandler(exchange -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }).webFilter(proxy).build();

        for (String path : permitAllPaths) {
            client.get().uri(path)
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Test
    @DisplayName("Petición GET o POST anónima a rutas protegidas debe ser rechazada con 401 y RFC 7807 con correlationId")
    void debeRechazarPeticionAnonimaCon401YRfc7807() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        ReactiveJwtDecoder decoder = token -> Mono.error(new BadCredentialsException("Token ausente o inválido"));
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(decoder)));

        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
        WebFilterChainProxy proxy = new WebFilterChainProxy(chain);

        WebTestClient client = WebTestClient.bindToWebHandler(exchange -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }).webFilter(proxy).build();

        // 1. GET protegido sin correlationId -> debe autogenerarlo
        client.get().uri("/v1/torneos")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectHeader().value("X-Correlation-ID", id -> {
                    assertNotNull(id);
                    assertFalse(id.isBlank());
                })
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://fulvo.com/errors/unauthorized")
                .jsonPath("$.title").isEqualTo("No Autorizado")
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.instance").isEqualTo("/v1/torneos")
                .jsonPath("$.correlationId").isNotEmpty();

        // 2. POST protegido con correlationId entrante -> debe propagarlo
        String corrId = "corr-gateway-401-custom";
        client.post().uri("/v1/torneos")
                .header("X-Correlation-ID", corrId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\": \"Torneo Clausura\"}")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectHeader().valueEquals("X-Correlation-ID", corrId)
                .expectBody()
                .jsonPath("$.correlationId").isEqualTo(corrId);
    }

    @Test
    @DisplayName("Petición autenticada con rol insuficiente (ROL_FANATICO intentando POST /v1/torneos) debe ser rechazada con 403 RFC 7807")
    void debeRechazarPeticionConRolInsuficienteCon403YRfc7807() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        ReactiveJwtDecoder decoder = token -> {
            Jwt jwt = Jwt.withTokenValue(token)
                    .header("alg", "RS256")
                    .claim("sub", "fanatico-1")
                    .claim("preferred_username", "fanatico1")
                    .claim("realm_access", Map.of("roles", List.of("ROL_FANATICO")))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            return Mono.just(jwt);
        };
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(decoder)));

        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
        WebFilterChainProxy proxy = new WebFilterChainProxy(chain);

        WebTestClient client = WebTestClient.bindToWebHandler(exchange -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }).webFilter(proxy).build();

        String corrId = "corr-gateway-403-insufficient";
        client.post().uri("/v1/torneos")
                .header("X-Correlation-ID", corrId)
                .header("Authorization", "Bearer valid-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\": \"Torneo Prohibido\"}")
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectHeader().valueEquals("X-Correlation-ID", corrId)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://fulvo.com/errors/forbidden")
                .jsonPath("$.title").isEqualTo("Acceso Denegado")
                .jsonPath("$.status").isEqualTo(403)
                .jsonPath("$.instance").isEqualTo("/v1/torneos")
                .jsonPath("$.correlationId").isEqualTo(corrId);
    }
}
