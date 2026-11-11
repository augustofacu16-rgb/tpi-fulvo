package com.fulvo.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Punto de entrada reactivo para peticiones no autenticadas (HTTP 401).
 * Devuelve un ProblemDetail estandarizado bajo RFC 7807 (Apuntes 18 y 23).
 */
@Slf4j
@Component
public class GatewayAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        String path = exchange.getRequest().getPath().value();
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        String correlationProperty = (correlationId != null && !correlationId.isBlank())
                ? ",\n  \"correlationId\": \"" + escapeJson(correlationId) + "\""
                : "";

        String json = """
                {
                  "type": "https://fulvo.com/errors/unauthorized",
                  "title": "No Autorizado",
                  "status": 401,
                  "detail": "El recurso solicitado requiere autenticación Bearer JWT válida.",
                  "instance": "%s",
                  "timestamp": "%s"%s
                }
                """.formatted(escapeJson(path), Instant.now().toString(), correlationProperty);

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
