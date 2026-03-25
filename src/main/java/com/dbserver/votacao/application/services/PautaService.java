package com.dbserver.votacao.application.services;

import com.dbserver.votacao.application.ports.out.PautaRepositoryPort;
import com.dbserver.votacao.application.ports.out.SessaoRepositoryPort;
import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import com.dbserver.votacao.domain.model.Pauta;
import com.dbserver.votacao.domain.model.Sessao;
import com.dbserver.votacao.domain.model.Voto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PautaService {

    private final PautaRepositoryPort pautaRepository;
    private final SessaoRepositoryPort sessaoRepository;
    private final VotoRepositoryPort votoRepository;

    public Pauta criarPauta(Pauta pauta) {
        log.info("M=criarPauta, status=START, titulo={}", pauta.getTitulo());
        Pauta pautaSalva = pautaRepository.salvar(pauta);
        log.info("M=criarPauta, status=SUCCESS, id={}", pautaSalva.getId());
        return pautaSalva;
    }

    public List<Pauta> listarPautas() {
        return pautaRepository.listarTodas();
    }

    public void abrirSessao(Long pautaId, Integer minutos) {
        Pauta pauta = pautaRepository.buscarPorId(pautaId)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada"));

        int minutosFinais = (minutos == null || minutos <= 0) ? 1 : minutos;

        Optional<Sessao> sessaoExistente = sessaoRepository.buscarPorPautaId(pautaId);

        Sessao sessao;
        if (sessaoExistente.isPresent()) {
            sessao = sessaoExistente.get();
            if (sessao.estaAberta()) {
                throw new RuntimeException("Sessão já está aberta para esta pauta");
            }
            log.info("M=abrirSessao, status=UPDATING, pautaId={}", pautaId);
        } else {
            sessao = new Sessao();
            sessao.setPautaId(pautaId);
            sessao.setDataAbertura(LocalDateTime.now());
        }

        sessao.setDataEncerramento(LocalDateTime.now().plusMinutes(minutosFinais));
        sessaoRepository.salvar(sessao);
    }

    public void receberVoto(Long pautaId, Voto voto) {
        log.info("M=receberVoto, status=START, pautaId={}, associadoId={}", pautaId, voto.getAssociadoId());

        // 1. Pauta existe?
        pautaRepository.buscarPorId(pautaId)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada"));

        // 2. Sessão está aberta?
        Sessao sessao = sessaoRepository.buscarPorPautaId(pautaId)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada para esta pauta"));

        if (!sessao.estaAberta()) {
            throw new RuntimeException("A sessão para esta pauta já está encerrada");
        }

        // 3. Associado já votou?
        if (votoRepository.existeVotoPorPautaEAssociado(pautaId, voto.getAssociadoId())) {
            throw new RuntimeException("Associado já votou nesta pauta");
        }

        // 4. Salvar voto
        voto.setPautaId(pautaId);
        votoRepository.salvar(voto);

        log.info("M=receberVoto, status=SUCCESS, pautaId={}, associadoId={}", pautaId, voto.getAssociadoId());
    }

    public com.dbserver.votacao.domain.model.ResultadoPauta obterResultado(Long pautaId) {
        log.info("M=obterResultado, status=START, pautaId={}", pautaId);

        // Opcional: Validar se a pauta existe
        pautaRepository.buscarPorId(pautaId)
                .orElseThrow(() -> new RuntimeException("Pauta não encontrada"));

        java.util.List<Voto> votos = votoRepository.buscarVotosPorPauta(pautaId);

        long totalSim = votos.stream()
                .filter(v -> v.getEscolha() == com.dbserver.votacao.domain.model.EscolhaVoto.SIM)
                .count();

        long totalNao = votos.stream()
                .filter(v -> v.getEscolha() == com.dbserver.votacao.domain.model.EscolhaVoto.NAO)
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

        return new com.dbserver.votacao.domain.model.ResultadoPauta(
                totalSim,
                totalNao,
                votos.size(),
                vencedor);
    }
}