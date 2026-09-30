package com.agenda.kanban.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI kanbanAgendaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tablero Kanban con Agenda y Espacios de Trabajo — API")
                        .version("v1")
                        .description("Ver specs/001-kanban-agenda-workspaces/contracts/api-contracts.md"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
