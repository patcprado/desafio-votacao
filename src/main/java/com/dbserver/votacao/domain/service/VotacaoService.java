package com.dbserver.votacao.domain.service;

import com.dbserver.votacao.domain.exception.BusinessException;
import com.dbserver.votacao.application.ports.out.CpfValidationPort;
import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotacaoService {

    private final VotoRepositoryPort votoRepository;
    private final CpfValidationPort cpfValidationPort;
    private final SessaoService sessaoService; // Para validar se está aberta

    public void registrarVoto(Long pautaId, String cpf, String votoValue) {
        log.info("Iniciando processo de voto - Pauta: {}, CPF: {}", pautaId, cpf);

        // 1. Validar se a sessão está aberta (Regra de Negócio)
        if (!sessaoService.isSessaoAberta(pautaId)) {
            throw new BusinessException("A sessão de votação para esta pauta está fechada.");
        }

        // 2. Performance (Bónus 2): Validar se já votou antes de chamar API externa
        if (votoRepository.existeVotoPorPautaEAssociado(pautaId, cpf)) {
            log.warn("Tentativa de voto duplicado detectada - CPF: {}", cpf);
            throw new BusinessException("Este CPF já votou nesta pauta.");
        }

        // 3. Integração (Bónus 1): Validar CPF no sistema externo
        if (!cpfValidationPort.isAbleToVote(cpf)) {
            log.warn("CPF não autorizado a votar pelo sistema externo: {}", cpf);
            throw new BusinessException("O associado não possui permissão para votar (UNABLE_TO_VOTE).");
        }

        // 4. Salvar o voto (Persistência)
        // Aqui você chamaria o seu adapter de persistência para salvar a VotoEntity
        log.info("Voto processado com sucesso para o CPF: {}", cpf);
    }
}