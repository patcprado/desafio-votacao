package com.dbserver.votacao.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PautaRequest(
        @NotBlank(message = "O título é obrigatório") String titulo,

        String descricao) {
}