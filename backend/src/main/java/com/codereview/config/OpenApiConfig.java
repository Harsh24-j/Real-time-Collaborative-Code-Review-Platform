package com.codereview.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI Configuration
 * Skills: API, Web Applications, Back-End Web Development
 *
 * Accessible at:
 * http://localhost:8080/swagger-ui.html â€” Interactive UI
 * http://localhost:8080/api-docs â€” Raw OpenAPI JSON
 *
 * All secured endpoints show a "Authorize" button in the UI.
 * Paste "Bearer <token>" after logging in to test protected routes.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // â”€â”€ API Metadata â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                .info(new Info()
                        .title("Zyncora API")
                        .description("""
                                AI-powered real-time collaborative Zyncora.

                                **Authentication**: Use `POST /api/auth/login` to obtain a JWT token,
                                then click **Authorize** and enter `Bearer <your-token>`.

                                **WebSocket**: Connect to `/ws-review` using SockJS + STOMP.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Harsh Shrivastava")
                                .email("harshshrivastava807@gmail.com")
                                .url("https://github.com/Harsh24-j"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // â”€â”€ Servers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development"),
                        new Server()
                                .url("https://api.codereview.example.com")
                                .description("AWS EC2 Production")))

                // â”€â”€ JWT Security Scheme â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login")))

                // Apply JWT requirement globally (overridable per-operation)
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
