package com.example.crafteria_server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        String[] paths = {"/api/v1/**"};
        return GroupedOpenApi.builder()
                .group("Crafteria Public API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public OpenAPI apiDocConfig() {
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token");

        return new OpenAPI()
                .info(new Info()
                        .title("Crafteria API")
                        .description("Crafteria API 명세서")
                        .version("1.0.0")
                ).components(new Components().addSecuritySchemes
                        ("Bearer Authentication", createAPIKeyScheme()))
                .addSecurityItem(securityRequirement);
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
