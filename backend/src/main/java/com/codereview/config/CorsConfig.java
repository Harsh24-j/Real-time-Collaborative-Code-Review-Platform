package com.codereview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Global CORS Configuration
 * Skills: Secure Coding, Back-End Web Development, Web Applications
 *
 * Provides a CorsFilter bean consumed by both Spring MVC and WebSocket.
 * The Security-layer CORS (in SecurityConfig) delegates to the same
 * CorsConfigurationSource to avoid conflicts.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsRaw;

    /**
     * CorsFilter â€” applied before the Spring Security filter chain
     * so that pre-flight OPTIONS requests are handled without authentication.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Origins from application.yml (comma-separated)
        config.setAllowedOrigins(Arrays.asList(allowedOriginsRaw.split(",")));

        // All standard REST methods + pre-flight
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Accept any request header
        config.setAllowedHeaders(List.of("*"));

        // Expose Authorization so the frontend can read the JWT from the response
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        // Allow cookies / Authorization header in cross-origin requests
        config.setAllowCredentials(true);

        // Cache pre-flight response for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
