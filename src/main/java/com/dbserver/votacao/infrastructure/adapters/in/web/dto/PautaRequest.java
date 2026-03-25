package com.dbserver.votacao.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PautaRequest(
        @NotBlank(message = "O título é obrigatório")
        @Schema(description = "Título da pauta para votação", example = "Modernização da frota de veículos")
        String titulo,

        @Schema(description = "Descrição detalhada do objetivo da pauta", example = "Votação para decidir a compra de 5 novos veículos elétricos.")
        String descricao
) {
}