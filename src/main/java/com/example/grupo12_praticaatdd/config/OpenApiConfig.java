package com.example.grupo12_praticaatdd.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentacao exposta em /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiEducacaoContinuadaGamificada() {
        return new OpenAPI().info(new Info()
                .title("Educacao Continuada Gamificada - API do Forum")
                .version("1.0.0")
                .description("""
                        API da US1 do Grupo 12: "EU COMO aluno assinante da plataforma QUERO ser o \
                        que mais escreve topicos e ajuda outros participantes no forum PARA ganhar \
                        um curso no final do mes."

                        A regra de premiacao fica no pacote `domain`, construido por TDD \
                        (ciclo RED / GREEN / BLUE, 100% de cobertura).""")
                .contact(new Contact().name("Grupo 12 - Caua Tolentino, Pedro Pizzi, Henry Tanaka")));
    }
}
