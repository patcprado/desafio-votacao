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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotacaoService {

    private final VotoRepositoryPort votoRepository;
    private final CpfValidationPort cpfValidationPort;
    private final SessaoService sessaoService;

    @Transactional
    public VotoResponse registrarVoto(Long pautaId, String cpf, String escolha) {
        log.info("[VOTAÇÃO] Iniciando processamento. Associado: {}, Pauta: {}", cpf, pautaId);

        // 1. Regra de Negócio Local (Rápida): A sessão está aberta?
        log.debug("[VOTAÇÃO] Validando status da sessão. Pauta: {}", pautaId);
        if (!sessaoService.isSessaoAberta(pautaId)) {
            log.error("[VOTAÇÃO-ERRO] Falha ao registrar voto. Motivo: {}, Contexto: [Assoc: {}, Pauta: {}]",
                    "Sessão fechada", cpf, pautaId);
            throw new BusinessException("A sessão de votação para esta pauta está fechada.");
        }

        // 2. Limpeza do CPF para os próximos passos
        String cpfLimpo = cpf.replaceAll("\\D", "");
        log.debug("[VOTAÇÃO] CPF sanitizado para consulta: {}", cpfLimpo);

        // 3. Integração Externa (Bônus): O CPF está apto para votar?
        log.debug("[VOTAÇÃO] Validando aptidão do CPF no serviço externo: {}", cpfLimpo);
        if (!cpfValidationPort.isAbleToVote(cpfLimpo)) {
            log.error("[VOTAÇÃO-ERRO] Falha ao registrar voto. Motivo: {}, Contexto: [Assoc: {}, Pauta: {}]",
                    "CPF inapto", cpfLimpo, pautaId);
            throw new CpfInaptoException("UNABLE_TO_VOTE");
        }

        // 4. Consulta ao Banco: Já existe voto?
        log.debug("[VOTAÇÃO] Verificando duplicidade de voto. Assoc: {}, Pauta: {}", cpfLimpo, pautaId);
        if (votoRepository.existeVotoPorPautaEAssociado(pautaId, cpfLimpo)) {
            log.error("[VOTAÇÃO-ERRO] Falha ao registrar voto. Motivo: {}, Contexto: [Assoc: {}, Pauta: {}]",
                    "Voto duplicado", cpfLimpo, pautaId);
            throw new VotoDuplicadoException(cpfLimpo, pautaId);
        }

        // 5. Persistência
        Voto novoVoto = new Voto();
        novoVoto.setPautaId(pautaId);
        novoVoto.setAssociadoId(cpfLimpo);
        novoVoto.setEscolha(com.dbserver.votacao.domain.model.EscolhaVoto.valueOf(escolha.toUpperCase()));

        votoRepository.salvar(novoVoto);

        log.info("[VOTAÇÃO] Voto processado com sucesso. Assoc: {}, Pauta: {}", cpfLimpo, pautaId);

        return new VotoResponse(
                cpfLimpo,
                "ABLE_TO_VOTE",
                escolha.toUpperCase(),
                true);
    }
}