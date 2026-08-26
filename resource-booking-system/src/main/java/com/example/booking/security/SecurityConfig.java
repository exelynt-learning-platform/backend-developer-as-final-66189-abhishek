package com.example.booking.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.sendError(
                                            HttpServletResponse.SC_UNAUTHORIZED,
                                            "Authentication required"
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                    response.sendError(
                                            HttpServletResponse.SC_FORBIDDEN,
                                            "Access denied"
                                    );
                                }
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC
                        // =========================

                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // =========================
                        // RESOURCES - USER + ADMIN READ
                        // =========================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/resources",
                                "/resources/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // =========================
                        // RESOURCES - ADMIN ONLY
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/resources",
                                "/resources/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/resources",
                                "/resources/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/resources",
                                "/resources/**"
                        ).hasRole("ADMIN")

                        // =========================
                        // RESERVATIONS
                        // =========================

                        .requestMatchers(
                                "/reservations",
                                "/reservations/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // =========================
                        // EVERYTHING ELSE
                        // =========================

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}