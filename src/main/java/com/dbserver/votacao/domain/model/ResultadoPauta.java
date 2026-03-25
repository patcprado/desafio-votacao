package com.dbserver.votacao.domain.model;

public record ResultadoPauta(
    long totalSim,
    long totalNao,
    long totalVotos,
    String vencedor
) {}
