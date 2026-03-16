package com.codereview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Standalone PasswordEncoder configuration.
 *
 * Extracted from SecurityConfig to break the circular dependency:
 * SecurityConfig → UserDetailsService (UserService)
 * UserService → PasswordEncoder (was in SecurityConfig)
 * SecurityConfig → JwtAuthFilter → UserService ← cycle!
 *
 * By placing PasswordEncoder in its own @Configuration class it is
 * created independently of SecurityConfig, removing all circular paths.
 *
 * Skills: Secure Coding, Spring Boot
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt password encoder — strength 12.
     * Used by UserService (registration / password change) and
     * SecurityConfig's DaoAuthenticationProvider.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
