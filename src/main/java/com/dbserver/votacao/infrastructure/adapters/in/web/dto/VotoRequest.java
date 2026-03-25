package com.dbserver.votacao.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VotoRequest(
        @NotBlank(message = "O ID do associado (CPF) é obrigatório")
        @Size(min = 11, max = 11, message = "O CPF deve ter exatamente 11 dígitos")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter apenas números")
        @Schema(description = "ID único do associado (CPF)", example = "12345678901")
        String associadoId,

        @NotBlank(message = "A escolha do voto (SIM/NAO) é obrigatória")
        @Schema(description = "Escolha do voto (SIM ou NAO)", example = "SIM", allowableValues = {"SIM", "NAO"})
        String escolha
) {
}