package com.fulvo.gateway.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KeycloakReactiveRoleConverter - Conversión de roles OIDC de Keycloak a autoridades Spring Security")
class KeycloakReactiveRoleConverterTest {

    private final KeycloakReactiveRoleConverter converter = new KeycloakReactiveRoleConverter();

    @Test
    @DisplayName("Debe convertir claims realm_access.roles a autoridades ROL_* y ROLE_*")
    void debeConvertirRolesKeycloakExitosamente() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", "usuario-123")
                .claim("preferred_username", "arbitro1")
                .claim("realm_access", Map.of("roles", List.of("ROL_ARBITRO", "ROL_FANATICO")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken authToken = converter.convert(jwt).block();

        assertNotNull(authToken);
        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        assertFalse(authorities.isEmpty());

        List<String> authorityNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();

        // Validar mapeo de ROL_ARBITRO
        assertTrue(authorityNames.contains("ROL_ARBITRO"), "Debe contener ROL_ARBITRO directo");
        assertTrue(authorityNames.contains("ROLE_ROL_ARBITRO"), "Debe contener ROLE_ROL_ARBITRO prefijado");
        assertTrue(authorityNames.contains("ROLE_ARBITRO"), "Debe contener ROLE_ARBITRO normalizado para hasRole('ARBITRO')");

        // Validar mapeo de ROL_FANATICO
        assertTrue(authorityNames.contains("ROL_FANATICO"), "Debe contener ROL_FANATICO directo");
        assertTrue(authorityNames.contains("ROLE_FANATICO"), "Debe contener ROLE_FANATICO normalizado");
    }

    @Test
    @DisplayName("Debe manejar tokens sin claim realm_access sin lanzar excepciones")
    void debeManejarJwtSinRealmAccess() {
        Jwt jwt = Jwt.withTokenValue("mock-token-sin-roles")
                .header("alg", "RS256")
                .claim("sub", "usuario-anonimo")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        AbstractAuthenticationToken authToken = converter.convert(jwt).block();

        assertNotNull(authToken);
        assertTrue(authToken.getAuthorities().isEmpty(), "No debe contener autoridades");
    }
}
