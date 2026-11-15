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
    @DisplayName("GatewayAuthenticationEntryPoint debe retornar HTTP 401, ProblemDetail RFC 7807 y propagar correlationId entrante")
    void debeRetornar401ConProblemDetailYCorrelationIdEntrante() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/v1/partidos/1/actas")
                .header("X-Correlation-ID", "corr-test-401")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        entryPoint.commence(exchange, new BadCredentialsException("Token ausente o inválido")).block();

        var response = exchange.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertEquals("corr-test-401", response.getHeaders().getFirst("X-Correlation-ID"));

        String body = extractResponseBody(exchange);
        assertTrue(body.contains("\"type\": \"https://fulvo.com/errors/unauthorized\""));
        assertTrue(body.contains("\"title\": \"No Autorizado\""));
        assertTrue(body.contains("\"status\": 401"));
        assertTrue(body.contains("\"detail\": \"El recurso solicitado requiere autenticación Bearer JWT válida.\""));
        assertTrue(body.contains("\"instance\": \"/v1/partidos/1/actas\""));
        assertTrue(body.contains("\"timestamp\""));
        assertTrue(body.contains("\"correlationId\": \"corr-test-401\""));
    }

    @Test
    @DisplayName("GatewayAuthenticationEntryPoint debe autogenerar X-Correlation-ID cuando la petición no lo incluye")
    void debeAutogenerarCorrelationIdEn401SiNoVieneEnRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/v1/torneos").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        entryPoint.commence(exchange, new BadCredentialsException("Token ausente")).block();

        var response = exchange.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        String generatedCorrelationId = response.getHeaders().getFirst("X-Correlation-ID");
        assertNotNull(generatedCorrelationId, "Debe inyectar header X-Correlation-ID en respuesta");
        assertFalse(generatedCorrelationId.isBlank());

        String body = extractResponseBody(exchange);
        assertTrue(body.contains("\"correlationId\": \"" + generatedCorrelationId + "\""));
    }

    @Test
    @DisplayName("GatewayAccessDeniedHandler debe retornar HTTP 403, ProblemDetail RFC 7807 y propagar correlationId entrante")
    void debeRetornar403ConProblemDetailYCorrelationIdEntrante() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/v1/partidos/1/actas")
                .header("X-Correlation-ID", "corr-test-403")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        accessDeniedHandler.handle(exchange, new AccessDeniedException("Rol insuficiente")).block();

        var response = exchange.getResponse();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertEquals("corr-test-403", response.getHeaders().getFirst("X-Correlation-ID"));

        String body = extractResponseBody(exchange);
        assertTrue(body.contains("\"type\": \"https://fulvo.com/errors/forbidden\""));
        assertTrue(body.contains("\"title\": \"Acceso Denegado\""));
        assertTrue(body.contains("\"status\": 403"));
        assertTrue(body.contains("\"detail\": \"El usuario autenticado no posee los privilegios requeridos para ejecutar esta operación.\""));
        assertTrue(body.contains("\"instance\": \"/v1/partidos/1/actas\""));
        assertTrue(body.contains("\"timestamp\""));
        assertTrue(body.contains("\"correlationId\": \"corr-test-403\""));
    }

    @Test
    @DisplayName("GatewayAccessDeniedHandler debe autogenerar X-Correlation-ID cuando la petición no lo incluye")
    void debeAutogenerarCorrelationIdEn403SiNoVieneEnRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.delete("/v1/sedes/10").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        accessDeniedHandler.handle(exchange, new AccessDeniedException("Rol insuficiente")).block();

        var response = exchange.getResponse();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        String generatedCorrelationId = response.getHeaders().getFirst("X-Correlation-ID");
        assertNotNull(generatedCorrelationId, "Debe inyectar header X-Correlation-ID en respuesta");
        assertFalse(generatedCorrelationId.isBlank());

        String body = extractResponseBody(exchange);
        assertTrue(body.contains("\"correlationId\": \"" + generatedCorrelationId + "\""));
    }

    private String extractResponseBody(MockServerWebExchange exchange) {
        DataBuffer buffer = exchange.getResponse().getBody().blockFirst();
        assertNotNull(buffer, "El cuerpo de la respuesta no debe ser nulo");
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
