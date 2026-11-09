package com.fulvo.gateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Convertidor reactivo de claims de Keycloak a GrantedAuthority de Spring Security.
 * Extrae los roles del claim 'realm_access.roles' y los expone con nombres directos
 * (ej. ROL_ORGANIZADOR) y con prefijo ROLE_ (ej. ROLE_ORGANIZADOR, ROLE_ROL_ORGANIZADOR)
 * para máxima interoperabilidad con hasRole y hasAuthority.
 */
@Component
public class KeycloakReactiveRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            for (String role : roles) {
                // Agregar autoridad literal (ej. "ROL_ORGANIZADOR")
                authorities.add(new SimpleGrantedAuthority(role));
                // Agregar con prefijo standard ROLE_ (ej. "ROLE_ROL_ORGANIZADOR")
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                // Si ya viene con prefijo ROL_, mapear también a ROLE_ORGANIZADOR para hasRole("ORGANIZADOR")
                if (role.startsWith("ROL_")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.substring(4)));
                }
            }
        }

        return authorities;
    }
}
