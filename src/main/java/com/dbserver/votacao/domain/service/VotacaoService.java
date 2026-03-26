package com.dbserver.votacao.domain.service;

import com.dbserver.votacao.application.ports.out.CpfValidationPort;
import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import com.dbserver.votacao.domain.exception.BusinessException;
import com.dbserver.votacao.domain.exception.CpfInaptoException;
import com.dbserver.votacao.domain.model.Voto;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.VotoResponse;
import com.dbserver.votacao.infrastructure.adapters.in.web.exception.VotoDuplicadoException;
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

    public VotoResponse registrarVoto(Long pautaId, String cpf, String escolha) {
        log.info("Iniciando registro de voto - Pauta: {}, CPF: {}", pautaId, cpf);

        // 1. Regra de Negócio Local (Rápida): A sessão está aberta?
        if (!sessaoService.isSessaoAberta(pautaId)) {
            throw new BusinessException("A sessão de votação para esta pauta está fechada.");
        }

        // 2. Limpeza do CPF para os próximos passos
        String cpfLimpo = cpf.replaceAll("\\D", "");

        // 3. Integração Externa (Bônus): O CPF está apto para votar?
        if (!cpfValidationPort.isAbleToVote(cpfLimpo)) {
            log.warn("Tentativa de voto com CPF inapto: {}", cpfLimpo);
            throw new CpfInaptoException("UNABLE_TO_VOTE");
        }

        // 4. Consulta ao Banco: Já existe voto?
        if (votoRepository.existeVotoPorPautaEAssociado(pautaId, cpfLimpo)) {
            log.warn("Tentativa de voto duplicado detectada - CPF: {} | Pauta: {}", cpfLimpo, pautaId);
            throw new VotoDuplicadoException(cpfLimpo, pautaId);
        }

        // 5. Persistência
        Voto novoVoto = new Voto();
        novoVoto.setPautaId(pautaId);
        novoVoto.setAssociadoId(cpfLimpo);
        novoVoto.setEscolha(com.dbserver.votacao.domain.model.EscolhaVoto.valueOf(escolha.toUpperCase()));

        votoRepository.salvar(novoVoto);

        log.info("Voto processado com sucesso para o CPF: {}", cpfLimpo);

        return new VotoResponse(
                cpfLimpo,
                "ABLE_TO_VOTE",
                escolha.toUpperCase(),
                true
        );
    }
}