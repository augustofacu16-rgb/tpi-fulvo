package com.fulvo.gateway.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Manejadores de Seguridad Perimetral Reactiva en API Gateway (RFC 7807)")
class GatewaySecurityHandlersTest {

    private final GatewayAuthenticationEntryPoint entryPoint = new GatewayAuthenticationEntryPoint();
    private final GatewayAccessDeniedHandler accessDeniedHandler = new GatewayAccessDeniedHandler();

    @Test
    @DisplayName("GatewayAuthenticationEntryPoint debe retornar HTTP 401 y ProblemDetail RFC 7807")
    void debeRetornar401ConProblemDetail() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/v1/partidos/1/actas")
                .header("X-Correlation-ID", "corr-test-401")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        entryPoint.commence(exchange, new BadCredentialsException("Token ausente o inválido")).block();

        var response = exchange.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());

        // Verificar body
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GatewayAccessDeniedHandler debe retornar HTTP 403 y ProblemDetail RFC 7807")
    void debeRetornar403ConProblemDetail() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/v1/partidos/1/actas")
                .header("X-Correlation-ID", "corr-test-403")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        accessDeniedHandler.handle(exchange, new AccessDeniedException("Rol insuficiente")).block();

        var response = exchange.getResponse();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
    }
}
