package com.codereview;

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
public class CodeReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewApplication.class, args);
        System.out.println("✅ Code Review Platform Started Successfully!");
        System.out.println("📚 Swagger UI: http://localhost:8080/swagger-ui.html");
    }
}
