package com.ShopSphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =========================
                // PUBLIC APIs
                // =========================

                .requestMatchers(
                    "/error",
                    "/api/health",
                    "/api/auth/**"
                ).permitAll()

                // Public product browsing (GET only)
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products",
                    "/api/products/*",
                    "/api/products/category/**"
                ).permitAll()

                // Public category browsing (GET only)
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/categories",
                    "/api/categories/*"
                ).permitAll()

                // Admin category management
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/categories"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/categories/*"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/categories/*"
                ).hasRole("ADMIN")

                // =========================
                // ADMIN APIs
                // =========================

                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")

                // =========================
                // SELLER APIs
                // =========================

                .requestMatchers("/api/seller/**")
                .hasRole("SELLER")

                .requestMatchers("/api/products/seller/**")
                .hasRole("SELLER")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products"
                ).hasRole("SELLER")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products/*"
                ).hasRole("SELLER")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/*"
                ).hasRole("SELLER")

                // =========================
                // BUYER APIs
                // =========================

                .requestMatchers("/api/buyer/**")
                .hasRole("BUYER")

                // =========================
                // LOGGED-IN USER
                // =========================

                .requestMatchers("/api/users/me")
                .authenticated()

                // =========================
                // EVERYTHING ELSE
                // =========================

                .anyRequest()
                .authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}