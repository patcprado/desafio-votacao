package com.dbserver.votacao.infrastructure.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupListener {
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent() {
        System.out.println("\n==========================================================");
        System.out.println("   SERVIÇO DE VOTAÇÃO ESTÁ NO AR!");
        System.out.println("   Acesse por: http://localhost:8080/v1/pautas");
        System.out.println("   Swagger: http://localhost:8080/swagger-ui/index.html");
        System.out.println("==========================================================\n");
    }
}