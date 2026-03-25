package com.dbserver.votacao.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResultadoPauta(
                @Schema(description = "Quantidade de votos SIM", example = "100") Long totalSim,

                @Schema(description = "Quantidade de votos NAO", example = "50") Long totalNao,

                @Schema(description = "Total de votos computados", example = "150") Long totalVotos,

                @Schema(description = "Resultado final da votação", example = "APROVADA") String resultado) {
}
