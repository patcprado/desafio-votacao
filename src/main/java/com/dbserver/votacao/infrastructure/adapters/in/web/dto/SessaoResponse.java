package com.dbserver.votacao.infrastructure.adapters.in.web.dto;

import java.time.LocalDateTime;

public record SessaoResponse(
        Long id,
        Long pautaId,
        LocalDateTime dataAbertura,
        LocalDateTime dataEncerramento,
        boolean estaAberta
) {}
