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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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

        // 1. Limpeza e Formatação
        String cpfLimpo = validarELimparCpf(voto.getAssociadoId());
        voto.setAssociadoId(cpfLimpo);

        // 2. Validação de Sessão (Regra de Negócio Crítica)
        Sessao sessao = sessaoRepository.buscarPorPautaId(pautaId)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada para esta pauta"));

        if (!sessao.estaAberta()) {
            throw new RuntimeException("A sessão para esta pauta já está encerrada");
        }

        // 3. Validação Externa (CPF Inapto)
        if (!cpfValidationPort.isAbleToVote(cpfLimpo)) {
            throw new RuntimeException("Associado não autorizado para votar (CPF inválido ou inapto)");
        }

        // 4. Salvar Voto (A Unique Constraint no banco garante a unicidade aqui)
        voto.setPautaId(pautaId);
        votoRepository.salvar(voto);

        // 5. Métrica e Log de Sucesso
        incrementarMetricaVoto(pautaId, voto.getEscolha());
        log.info("M=receberVoto, status=SUCCESS, pautaId={}, associadoId={}", pautaId, cpfLimpo);
    }

    private void incrementarMetricaVoto(Long pautaId, EscolhaVoto escolha) {
        meterRegistry.counter("votacao_votos_total",
                        "pautaId", pautaId.toString(),
                        "escolha", escolha.name())
                .increment();
    }

    // Método auxiliar para deixar o código principal limpo
    private String validarELimparCpf(String cpf) {
        if (cpf == null || cpf.matches(".*[a-zA-Z].*")) {
            throw new RuntimeException("CPF deve conter apenas números");
        }
        return cpf.replaceAll("\\D", "");
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