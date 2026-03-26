package com.dbserver.votacao.application.services;

import com.dbserver.votacao.application.ports.out.PautaRepositoryPort;
import com.dbserver.votacao.application.ports.out.SessaoRepositoryPort;
import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import com.dbserver.votacao.domain.exception.BusinessException;
import com.dbserver.votacao.domain.model.EscolhaVoto;
import com.dbserver.votacao.domain.model.Pauta;
import com.dbserver.votacao.domain.model.ResultadoPauta;
import com.dbserver.votacao.domain.model.Sessao;
import com.dbserver.votacao.domain.model.Voto;
import com.dbserver.votacao.application.ports.out.CpfValidationPort;
import com.dbserver.votacao.infrastructure.adapters.in.web.exception.VotoDuplicadoException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.dbserver.votacao.infrastructure.adapters.in.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock private PautaRepositoryPort pautaRepository;
    @Mock private SessaoRepositoryPort sessaoRepository;
    @Mock private VotoRepositoryPort votoRepository;
    @Mock private CpfValidationPort cpfValidationPort;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks private PautaService pautaService;

    // Constantes para limpar o código e evitar repetição
    private static final Long PAUTA_ID = 1L;
    private static final String CPF_VALIDO = "12345678901";
    private static final String CPF_LONGO = "12345678901234";
    private static final String CPF_COM_LETRAS = "12345678e01";

    @BeforeEach
    void setup() {
        // Mock do Micrometer (Métricas) para não quebrar os testes de sucesso
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Nested
    @DisplayName("Caminho Feliz: Processamento de Pautas e Votos")
    class CaminhoFeliz {

        @Test
        @DisplayName("Deve abrir sessão com tempo padrão (1 min) quando minutos for nulo")
        void deveAbrirSessaoComTempoDefault() {
            Pauta pauta = new Pauta(PAUTA_ID, "Título", "Desc");
            when(pautaRepository.buscarPorId(PAUTA_ID)).thenReturn(Optional.of(pauta));
            when(sessaoRepository.buscarPorPautaId(PAUTA_ID)).thenReturn(Optional.empty());

            pautaService.abrirSessao(PAUTA_ID, null);

            ArgumentCaptor<Sessao> sessaoCaptor = ArgumentCaptor.forClass(Sessao.class);
            verify(sessaoRepository).salvar(sessaoCaptor.capture());

            assertTrue(sessaoCaptor.getValue().getDataEncerramento().isAfter(LocalDateTime.now()));
        }

        @Test
        @DisplayName("Deve receber voto com sucesso (Valida CPF, Duplicidade e Métricas)")
        void deveReceberVotoComSucesso() {
            Voto voto = new Voto(null, PAUTA_ID, CPF_VALIDO, EscolhaVoto.SIM);

            // Simula sessão aberta e CPF autorizado
            when(sessaoRepository.buscarPorPautaId(PAUTA_ID)).thenReturn(Optional.of(criarSessaoAberta()));
            when(cpfValidationPort.isAbleToVote(CPF_VALIDO)).thenReturn(true);
            when(votoRepository.existeVotoPorPautaEAssociado(PAUTA_ID, CPF_VALIDO)).thenReturn(false);

            pautaService.receberVoto(PAUTA_ID, voto);
            ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
            verify(votoRepository).salvar(votoCaptor.capture());
            assertEquals(CPF_VALIDO, votoCaptor.getValue().getAssociadoId());

            verify(meterRegistry).counter(anyString(), any(String[].class));
        }

        @Test
        @DisplayName("Deve calcular resultado com vitória do SIM")
        void deveCalcularResultadoComVitoriaSim() {
            List<Voto> votos = List.of(
                    new Voto(1L, PAUTA_ID, "A1", EscolhaVoto.SIM),
                    new Voto(2L, PAUTA_ID, "A2", EscolhaVoto.SIM),
                    new Voto(3L, PAUTA_ID, "A3", EscolhaVoto.NAO));

            when(pautaRepository.buscarPorId(PAUTA_ID)).thenReturn(Optional.of(new Pauta(PAUTA_ID, "T", "D")));
            when(votoRepository.buscarVotosPorPauta(PAUTA_ID)).thenReturn(votos);

            ResultadoPauta resultado = pautaService.obterResultado(PAUTA_ID);

            assertEquals(2L, resultado.totalSim());
            assertEquals(1L, resultado.totalNao());
            assertEquals("SIM", resultado.resultado());
        }
    }

    @Nested
    @DisplayName("Caminho Ruim: Validações de Sessão e Pauta")
    class CaminhoRuimSessao {

        @Test
        @DisplayName("Deve falhar ao abrir sessão para pauta inexistente")
        void deveFalharAbrirSessaoPautaInexistente() {
            when(pautaRepository.buscarPorId(PAUTA_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> pautaService.abrirSessao(PAUTA_ID, 10));
        }

        @Test
        @DisplayName("Deve falhar ao tentar abrir sessão que já está ativa")
        void deveFalharAbrirSessaoJaAtiva() {
            when(pautaRepository.buscarPorId(PAUTA_ID)).thenReturn(Optional.of(new Pauta(PAUTA_ID, "T", "D")));
            when(sessaoRepository.buscarPorPautaId(PAUTA_ID)).thenReturn(Optional.of(criarSessaoAberta()));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> pautaService.abrirSessao(PAUTA_ID, 5));

            assertEquals("Já existe uma sessão aberta para esta pauta.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Caminho Ruim: Validações e Restrições de Voto")
    class CaminhoRuimVoto {

        @Test
        @DisplayName("Deve rejeitar voto se o CPF já votou (Voto Duplicado: 409 Conflict)")
        void deveFalharVotoDuplicado() {
            Voto voto = new Voto(null, PAUTA_ID, CPF_VALIDO, EscolhaVoto.SIM);

            when(sessaoRepository.buscarPorPautaId(PAUTA_ID)).thenReturn(Optional.of(criarSessaoAberta()));
            when(cpfValidationPort.isAbleToVote(CPF_VALIDO)).thenReturn(true);
            when(votoRepository.existeVotoPorPautaEAssociado(PAUTA_ID, CPF_VALIDO)).thenReturn(true);

            assertThrows(VotoDuplicadoException.class, () -> pautaService.receberVoto(PAUTA_ID, voto));
        }

        @Test
        @DisplayName("Deve falhar se o CPF for inválido (Letras ou Tamanho)")
        void deveFalharCpfComLetras() {
            Voto voto = new Voto(null, PAUTA_ID, CPF_COM_LETRAS, EscolhaVoto.SIM);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> pautaService.receberVoto(PAUTA_ID, voto));

            assertEquals("CPF deve conter apenas números", ex.getMessage());
            verify(cpfValidationPort, never()).isAbleToVote(anyString());
        }

        @Test
        @DisplayName("Deve falhar se o serviço externo de CPF retornar 404 (Não Encontrado)")
        void deveFalharCpfNaoEncontradoExternamente() {
            Voto voto = new Voto(null, PAUTA_ID, "00000000000", EscolhaVoto.NAO);
            when(sessaoRepository.buscarPorPautaId(PAUTA_ID)).thenReturn(Optional.of(criarSessaoAberta()));
            when(cpfValidationPort.isAbleToVote("00000000000"))
                    .thenThrow(new ResourceNotFoundException("CPF inválido ou não encontrado."));

            assertThrows(ResourceNotFoundException.class, () -> pautaService.receberVoto(PAUTA_ID, voto));
        }
    }

    @Nested
    @DisplayName("Caminho Ruim: Resultados Atípicos")
    class CaminhoRuimResultado {

        @Test
        @DisplayName("Deve retornar EMPATE quando houver empate técnico (1x1)")
        void deveRetornarEmpateVotosIguais() {
            List<Voto> votos = List.of(
                    new Voto(1L, PAUTA_ID, "A1", EscolhaVoto.SIM),
                    new Voto(2L, PAUTA_ID, "A2", EscolhaVoto.NAO));

            when(pautaRepository.buscarPorId(PAUTA_ID)).thenReturn(Optional.of(new Pauta(PAUTA_ID, "T", "D")));
            when(votoRepository.buscarVotosPorPauta(PAUTA_ID)).thenReturn(votos);

            assertEquals("EMPATE", pautaService.obterResultado(PAUTA_ID).resultado());
        }

        @Test
        @DisplayName("Deve retornar EMPATE quando a pauta não tiver nenhum voto")
        void deveRetornarEmpateSemVotos() {
            when(pautaRepository.buscarPorId(PAUTA_ID)).thenReturn(Optional.of(new Pauta(PAUTA_ID, "T", "D")));
            when(votoRepository.buscarVotosPorPauta(PAUTA_ID)).thenReturn(Collections.emptyList());

            assertEquals("EMPATE", pautaService.obterResultado(PAUTA_ID).resultado());
        }
    }

    // Método auxiliar para criar uma sessão válida
    private Sessao criarSessaoAberta() {
        return new Sessao(1L, PAUTA_ID, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(10));
    }
}