package com.dbserver.votacao.domain.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sessao {
    private Long id;
    private Long pautaId;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;

    @JsonProperty("estaAberta")
    public boolean estaAberta() {
        return LocalDateTime.now().isBefore(dataEncerramento);
    }
}