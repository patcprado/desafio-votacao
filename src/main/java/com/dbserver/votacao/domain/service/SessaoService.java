package com.dbserver.votacao.domain.service;

import com.dbserver.votacao.infrastructure.adapters.in.web.exception.ResourceNotFoundException;
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
        return sessaoRepository.findFirstByPautaIdOrderByIdDesc(pautaId)
                .map(sessao -> LocalDateTime.now().isBefore(sessao.getDataEncerramento()))
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma sessão foi aberta para a pauta: " + pautaId));
    }
}