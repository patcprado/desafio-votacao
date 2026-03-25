package com.dbserver.votacao.application.ports.out;

import com.dbserver.votacao.domain.model.Sessao;

import java.util.Optional;

public interface SessaoRepositoryPort {
    Sessao salvar(Sessao sessao);
    Optional<Sessao> buscarPorPautaId(Long pautaId);
}