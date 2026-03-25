package com.dbserver.votacao.domain.service;

import com.dbserver.votacao.infrastructure.adapters.out.persistence.SessaoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessaoService {

    private final SessaoJpaRepository sessaoRepository;

    public boolean isSessaoAberta(Long pautaId) {
        log.info("Verificando status da sessão para a pauta: {}", pautaId);

        return sessaoRepository.findFirstByPautaIdOrderByIdDesc(pautaId)
                .map(sessao -> {
                    boolean ativa = LocalDateTime.now().isBefore(sessao.getDataEncerramento());
                    if (!ativa) {
                        log.warn("Sessão para a pauta {} já está encerrada.", pautaId);
                    }
                    return ativa;
                })
                .orElse(false); // Se não existe sessão, está fechada por padrão
    }
}