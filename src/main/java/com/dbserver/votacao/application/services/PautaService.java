package com.dbserver.votacao.application.services;

import com.dbserver.votacao.application.ports.out.PautaRepositoryPort;
import com.dbserver.votacao.application.ports.out.SessaoRepositoryPort;
import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import com.dbserver.votacao.domain.model.*;
import com.dbserver.votacao.application.ports.out.CpfValidationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PautaService {

    private final PautaRepositoryPort pautaRepository;
    private final SessaoRepositoryPort sessaoRepository;
    private final VotoRepositoryPort votoRepository;
    private final CpfValidationPort cpfValidationPort;
    private final MeterRegistry meterRegistry;

    public Pauta criarPauta(Pauta pauta) {
        log.info("M=criarPauta, status=START, titulo={}", pauta.getTitulo());
        pauta = pautaRepository.salvar(pauta);
        log.info("M=criarPauta, status=SUCCESS, id={}", pauta.getId());
        return pauta;
    }

    public List<Pauta> listarPautas() {
        return pautaRepository.listarTodas();
    }

    public void abrirSessao(Long pautaId, Integer minutos) {
        log.info("M=abrirSessao, status=START, pautaId={}", pautaId);

        pautaRepository.buscarPorId(pautaId)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada"));

        sessaoRepository.buscarPorPautaId(pautaId).ifPresent(sessao -> {
            if (sessao.estaAberta()) {
                throw new RuntimeException("Sessão já está aberta para esta pauta");
            }
        });

        int minutosFinais = (minutos == null || minutos <= 0) ? 1 : minutos;

        Sessao novaSessao = new Sessao();
        novaSessao.setPautaId(pautaId);
        novaSessao.setDataAbertura(LocalDateTime.now());
        novaSessao.setDataEncerramento(LocalDateTime.now().plusMinutes(minutosFinais));

        sessaoRepository.salvar(novaSessao);
        log.info("M=abrirSessao, status=SUCCESS, pautaId={}", pautaId);
    }

    public void receberVoto(Long pautaId, Voto voto) {
        log.info("M=receberVoto, status=START, pautaId={}, associadoId={}", pautaId, voto.getAssociadoId());

        if (voto.getAssociadoId() != null && voto.getAssociadoId().matches(".*[a-zA-Z].*")) {
             throw new RuntimeException("CPF deve conter apenas números");
        }

        String cpfLimpo = voto.getAssociadoId().replaceAll("\\D", "");
        voto.setAssociadoId(cpfLimpo);

        // 1. Pauta existe?
        pautaRepository.buscarPorId(pautaId)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada"));

        // 2. Sessão está aberta?
        Sessao sessao = sessaoRepository.buscarPorPautaId(pautaId)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada para esta pauta"));

        if (!sessao.estaAberta()) {
            throw new RuntimeException("A sessão para esta pauta já está encerrada");
        }

        if (!cpfValidationPort.isAbleToVote(voto.getAssociadoId())) {
            throw new RuntimeException("Associado não autorizado para votar (CPF inválido ou inapto)");
        }

        // 3. Associado já votou? (Aqui você já limpou o caminho)
        if (votoRepository.existeVotoPorPautaEAssociado(pautaId, voto.getAssociadoId())) {
            throw new RuntimeException("Associado já votou nesta pauta");
        }

        // 4. Salvar voto
        voto.setPautaId(pautaId);
        votoRepository.salvar(voto);

        // Métrica customizada para o Item 1 (Monitoramento)
        meterRegistry.counter("votacao_votos_total",
                        "pautaId", pautaId.toString(),
                        "escolha", voto.getEscolha().name())
                .increment();

        log.info("M=receberVoto, status=SUCCESS, pautaId={}, associadoId={}", pautaId, voto.getAssociadoId());
    }
    public ResultadoPauta obterResultado(Long pautaId) {
        log.info("M=obterResultado, status=START, pautaId={}", pautaId);

        // Opcional: Validar se a pauta existe
        pautaRepository.buscarPorId(pautaId)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada"));

        java.util.List<Voto> votos = votoRepository.buscarVotosPorPauta(pautaId);

        long totalSim = votos.stream()
                .filter(v -> v.getEscolha() == EscolhaVoto.SIM)
                .count();

        long totalNao = votos.stream()
                .filter(v -> v.getEscolha() == EscolhaVoto.NAO)
                .count();

        String vencedor;
        if (totalSim > totalNao) {
            vencedor = "SIM";
        } else if (totalNao > totalSim) {
            vencedor = "NAO";
        } else {
            vencedor = "EMPATE";
        }

        log.info("M=obterResultado, status=SUCCESS, pautaId={}", pautaId);

        return new ResultadoPauta(
                totalSim,
                totalNao,
                (long) votos.size(),
                vencedor);
    }
}