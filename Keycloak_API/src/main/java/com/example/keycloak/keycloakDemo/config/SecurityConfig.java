package com.example.keycloak.keycloakDemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


@Configuration
public class SecurityConfig {

//    https://www.baeldung.com/spring-security-openid-connect
//    https://www.baeldung.com/spring-boot-keycloak
//    https://www.baeldung.com/spring-security-map-authorities-jwt
//    https://www.baeldung.com/spring-security-oauth-resource-server


    private final static String HOME =  "http://localhost:8081";
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//              configures the access restriction policies
                .authorizeHttpRequests(authRequest -> authRequest
                        .requestMatchers(HttpMethod.PUT, "/api/users").hasAnyAuthority("admin")
                        .anyRequest().authenticated()
                )
//              configures what type of token is accepted, and how to determine the authorities from its payload
                .oauth2ResourceServer(oauthResource -> oauthResource
                        .jwt()
                        .jwtAuthenticationConverter(authenticationConverter())
                )
//              configures authentication support using an OAuth 2.0 and/or OpenID Connect Provider
                .oauth2Login(login -> login
                        .defaultSuccessUrl(HOME, true)
                )
//              configures the logout flow
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .permitAll()
                )
        ;
        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(
                        this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(HOME);
        return oidcLogoutSuccessHandler;
    }

    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(source -> {
            Collection<String> roles = Collection.class.cast(
                    Map.class.cast(source.getClaims().getOrDefault("realm_access", Map.of())).getOrDefault("roles", Collections.EMPTY_LIST)
            );

            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        });
        return jwtAuthenticationConverter;
    }

}
