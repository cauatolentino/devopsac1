package org.example.grupo12_praticaabb.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("API de Gamificação - Grupo 12")
                .version("1.0.0")
                .description("API para cálculo do engajamento dos alunos. Regra: tópicos + comentários de ajuda."));
    }
}
