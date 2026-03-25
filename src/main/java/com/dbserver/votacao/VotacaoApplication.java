package com.dbserver.votacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VotacaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(VotacaoApplication.class, args);
    }
}