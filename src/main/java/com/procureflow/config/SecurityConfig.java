
package com.procureflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role = jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return List.of();
            }

            return List.of(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role
                    )
            );
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                // REST API + JWT = stateless
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/index.html", "/styles.css", "/app.js", "/favicon.ico").permitAll()

                        // Authentication endpoints are public
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // Admin area
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // User's own profile
                        .requestMatchers(
                                "/api/users/me"
                        ).authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/procurement/intake/mine").authenticated()

                        // Procurement intake can be submitted by
                        // employees and other authenticated business roles
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/procurement/intake"
                        ).hasAnyRole(
                                "EMPLOYEE",
                                "PROCUREMENT",
                                "MANAGER",
                                "FINANCE",
                                "ADMIN"
                        )

                        // All other procurement endpoints
                        // remain restricted to Procurement and Admin
                        .requestMatchers(
                                "/api/procurement/**"
                        ).hasAnyRole(
                                "PROCUREMENT",
                                "ADMIN"
                        )

                        // Supplier endpoints
                        // Restricted to Procurement and Admin
                        .requestMatchers(
                                "/api/suppliers/**"
                        ).hasAnyRole(
                                "PROCUREMENT",
                                "ADMIN"
                        )

                        // Procurement policy endpoints
                        // Restricted to Procurement and Admin
                        .requestMatchers(
                                "/api/policies/**"
                        ).hasAnyRole(
                                "PROCUREMENT",
                                "ADMIN"
                        )

                        // Manager endpoints
                        .requestMatchers(
                                "/api/manager/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "ADMIN"
                        )

                        // Finance endpoints
                        .requestMatchers(
                                "/api/finance/**"
                        ).hasAnyRole(
                                "FINANCE",
                                "ADMIN"
                        )

                        // Employee endpoints
                        .requestMatchers(
                                "/api/employee/**"
                        ).hasAnyRole(
                                "EMPLOYEE",
                                "PROCUREMENT",
                                "MANAGER",
                                "FINANCE",
                                "ADMIN"
                        )

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://localhost:3000"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}

