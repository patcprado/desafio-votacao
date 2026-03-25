package com.dbserver.votacao.infrastructure.adapters.in.web.dto;

import com.dbserver.votacao.domain.model.EscolhaVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VotoRequest(
    @NotBlank
    String associadoId,

    @NotNull
    EscolhaVoto escolha
) {}
