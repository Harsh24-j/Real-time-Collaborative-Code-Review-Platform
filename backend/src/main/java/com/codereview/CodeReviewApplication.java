package com.codereview;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Application Class - Entry Point
 * Skills Demonstrated: Spring Boot, Server Side, Back-End Web Development
 */
@SpringBootApplication
@EnableJpaAuditing  // Enable automatic timestamp management
@EnableAsync        // Enable async processing
@Slf4j
public class CodeReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewApplication.class, args);
        log.info("✅ Code Review Platform Started Successfully!");
        log.info("📚 Swagger UI: http://localhost:8080/swagger-ui.html");
    }
}
