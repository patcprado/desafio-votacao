package com.dbserver.votacao.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VotoRequest(
                @NotBlank(message = "O ID do associado (CPF) é obrigatório") @Pattern(regexp = "(^\\d{11}$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$)", message = "O CPF deve ter 11 dígitos ou o formato 000.000.000-00") @Schema(description = "ID único do associado (CPF)", example = "12345678901") String associadoId,

                @NotBlank(message = "A escolha do voto (SIM/NAO) é obrigatória") @Pattern(regexp = "^(?i)(SIM|NAO)$", message = "A escolha deve ser apenas SIM ou NAO") @Schema(description = "Escolha do voto (SIM ou NAO)", example = "SIM", allowableValues = {
                                "SIM", "NAO" }) String escolha) {
}