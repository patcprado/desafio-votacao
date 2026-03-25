package com.dbserver.votacao.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Desafio Votação - API de Assembleias")
                        .version("v1.0")
                        .description("## API para gestão de pautas e sessões de votação em cooperativas.\n\n" +
                                "### Documentação Visual\n" +
                                "![Fluxo de Votação](http://localhost:8080/doc-assets/img1.JPG)\n\n" +
                                "Abaixo você pode testar os endpoints de criação de pauta, abertura de sessão e contabilização de votos.")
                        .contact(new Contact()
                                .name("Patricia Prado")
                                .url("https://github.com/patcprado")));
    }
}