package com.dbserver.votacao.domain.service;

import com.dbserver.votacao.domain.exception.BusinessException;
import com.dbserver.votacao.application.ports.out.CpfValidationPort;
import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import com.dbserver.votacao.domain.exception.CpfInaptoException;
import com.dbserver.votacao.domain.model.Voto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotacaoService {

    private final VotoRepositoryPort votoRepository;
    private final CpfValidationPort cpfValidationPort;
    private final SessaoService sessaoService;
    public void registrarVoto(Long pautaId, String cpf, String escolha) {
        log.info("Iniciando registro de voto - Pauta: {}, CPF: {}", pautaId, cpf);

        // 1. Validar Sessão
        if (!sessaoService.isSessaoAberta(pautaId)) {
            throw new BusinessException("A sessão de votação para esta pauta está fechada.");
        }

        // 2. Limpeza do CPF para busca e salvamento
        String cpfLimpo = cpf.replaceAll("\\D", "");

        // 3. Performance: Validar duplicidade local usando o CPF limpo
        if (votoRepository.existeVotoPorPautaEAssociado(pautaId, cpfLimpo)) {
            throw new BusinessException("Voto já registrado para este CPF nesta pauta.");
        }

        // 4. Integração: Validar CPF (Bônus 1)
        if (!cpfValidationPort.isAbleToVote(cpfLimpo)) {
            throw new CpfInaptoException("UNABLE_TO_VOTE");
        }

        // 5. PERSISTÊNCIA EFETIVA - Criando o objeto de domínio corretamente
        Voto novoVoto = new Voto();
        novoVoto.setPautaId(pautaId);
        novoVoto.setAssociadoId(cpfLimpo);

        // Convertendo a string "SIM" ou "NAO" do parâmetro 'escolha' para o Enum
        novoVoto.setEscolha(com.dbserver.votacao.domain.model.EscolhaVoto.valueOf(escolha.toUpperCase()));

        // Salvando no Banco
        votoRepository.salvar(novoVoto);

        log.info("Voto processado com sucesso para o CPF: {}", cpfLimpo);
    }
}