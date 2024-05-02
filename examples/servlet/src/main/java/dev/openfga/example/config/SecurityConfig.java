package dev.openfga.example.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Rlc3QuYXV0aDAuY29tLyIsInN1YiI6ImFubmUiLCJhdWQiOiJodHRwczovL3F1aWNrc3RhcnRzL2FwaSIsImlhdCI6MTcxNDY1NzE5OCwiZXhwIjoxNzE0NzQzNTk4fQ.wbpLJa5Q1196V2Of--XuvGc-ONHNZOZTSrwWLoD3isQ";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(customizer -> customizer.anyRequest().authenticated())
                .csrf(CsrfConfigurer::disable)
                
                // Add a JwtAuthenticationToken to the request to simulate using the JWT's subject
                // claim as the principal in the FGA check.
                // NOTE: This does NOT provide any JWT-based security - a real application would
                // also need to configure Spring Security as a resource server using JWT:
                // https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
                .addFilterBefore(authenticationFilter(), SecurityContextHolderFilter.class)
                .build();
    }

    private AuthenticationFilter authenticationFilter() {
        AuthenticationFilter filter = new AuthenticationFilter(authenticationManager(), new AuthenticationConverter() {
            @Override
            public Authentication convert(HttpServletRequest request) {
                Jwt jwt = Jwt.withTokenValue(TOKEN)
                        .claims(c -> {
                            c.put("iss","https://test.auth0.com/" );
                            // The subject will be the principal's name, used by default for the FGA check
                            c.put("sub", "anne");
                            c.put("aud", "https://mycompany/api");
                            c.put("iat", Instant.ofEpochSecond(1714657198));
                            c.put("exp", Instant.ofEpochSecond(1714657198).plus(10, ChronoUnit.DAYS));
                        })
                        .headers(h -> {
                            h.put("alg", "HS256");
                            h.put("type", "JWT");
                        })
                        .build();

                return new JwtAuthenticationToken(jwt);
            }
        });
        filter.setSuccessHandler((request, response, auth) -> {});
        return filter;
    }

    private AuthenticationManager authenticationManager() {
        return authentication -> new JwtAuthenticationToken(
                ((Jwt) authentication.getPrincipal()),
                null
        );
    }
}