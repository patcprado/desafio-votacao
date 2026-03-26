package com.dbserver.votacao.infrastructure.adapters.in.web.dto;

public record VotoResponse(
        String associadoId,
        String statusAssociado,
        String escolha,
        boolean votoEfetuado
) {}
