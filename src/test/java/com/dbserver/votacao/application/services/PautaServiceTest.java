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

    @Mock
    private PautaRepositoryPort pautaRepository;

    @Mock
    private SessaoRepositoryPort sessaoRepository;

    @Mock
    private VotoRepositoryPort votoRepository;

    @InjectMocks
    private PautaService pautaService;

    @Mock
    private CpfValidationPort cpfValidationPort;
    
    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;
    
    @BeforeEach
    public void setup() {
        // Para os testes que chamam o meterRegistry
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    // 1. Ajuste no teste de Pauta Inexistente (Abrir Sessão)
    @Test
    @DisplayName("Deve lançar exceção ao abrir sessão para pauta inexistente")
    public void deveLancarExcecaoAoAbrirSessaoParaPautaInexistente() {
        Long pautaId = 99L;
        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.empty());

        // Mude de RuntimeException para ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> pautaService.abrirSessao(pautaId, 10));
    }
    // 2. Ajuste no teste de Sessão já Aberta
    @Test
    public void deveLancarExcecaoAoAbrirSessaoJaAtiva() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        Sessao sessaoAtiva = new Sessao(1L, pautaId, LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoAtiva));

        // MUDANÇA: Agora esperamos BusinessException
        BusinessException exception = assertThrows(BusinessException.class,
                () -> pautaService.abrirSessao(pautaId, 5));

        assertEquals("Já existe uma sessão aberta para esta pauta.", exception.getMessage());
        verify(sessaoRepository, never()).salvar(any());
    }

    // 3. Ajuste no teste de Voto (Sessão Fechada)
    @Test
    public void deveLancarExcecaoAoVotarEmSessaoExpirada() {
        Long pautaId = 1L;
        Voto voto = new Voto(null, pautaId, "12345678901", EscolhaVoto.SIM);

        Sessao sessaoExpirada = new Sessao(1L, pautaId,
                LocalDateTime.now().minusMinutes(20),
                LocalDateTime.now().minusMinutes(10));

        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoExpirada));

        // MUDANÇA: Esperamos BusinessException ou a que você definiu no Service
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pautaService.receberVoto(pautaId, voto));

        assertTrue(exception.getMessage().contains("encerrada"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar votar duas vezes na mesma pauta")
    public void deveLancarExcecaoAoVotarDuplicado() {
        // GIVEN
        Long pautaId = 1L;
        String cpf = "12345678901";
        Voto voto = new Voto(null, pautaId, cpf, EscolhaVoto.SIM);

        // 1. Mock da sessão (OK)
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoAberta()));

        // 2. O PULO DO GATO: Autorizar o CPF para ele não barrar o teste aqui!
        when(cpfValidationPort.isAbleToVote(cpf)).thenReturn(true);

        // 3. Mock da duplicidade (Agora com o nome certo da sua Interface!)
        when(votoRepository.existeVotoPorPautaEAssociado(anyLong(), anyString())).thenReturn(true);

        // WHEN / THEN
        assertThrows(VotoDuplicadoException.class, () -> {
            pautaService.receberVoto(pautaId, voto);
        });
    }

    @Test
    public void deveAbrirSessaoComTempoDefault() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.empty());

        pautaService.abrirSessao(pautaId, null);

        ArgumentCaptor<Sessao> sessaoCaptor = ArgumentCaptor.forClass(Sessao.class);
        verify(sessaoRepository).salvar(sessaoCaptor.capture());

        Sessao sessaoSalva = sessaoCaptor.getValue();
        // Tempo default é 1 minuto
        assertTrue(sessaoSalva.getDataEncerramento().isAfter(LocalDateTime.now().plusSeconds(50)));
        assertTrue(sessaoSalva.getDataEncerramento().isBefore(LocalDateTime.now().plusMinutes(1).plusSeconds(10)));
    }

    @Test
    public void deveReceberVotoComSucesso() {
        // GIVEN
        Long pautaId = 1L;
        String cpf = "12345678901";
        Voto voto = new Voto(null, pautaId, cpf, EscolhaVoto.SIM);

        // Sessão ativa (Esta chamada ainda existe no Service)
        Sessao sessaoAtiva = new Sessao(1L, pautaId, LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(10));

        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoAtiva));
        when(cpfValidationPort.isAbleToVote(cpf)).thenReturn(true);

        pautaService.receberVoto(pautaId, voto);

        ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).salvar(votoCaptor.capture());

        assertEquals(cpf, votoCaptor.getValue().getAssociadoId());
        assertEquals(pautaId, votoCaptor.getValue().getPautaId());

        // Verifica se a métrica foi chamada
        verify(meterRegistry).counter(anyString(), any(String[].class));
    }

    @Test
    public void deveLancarExcecaoAoVotarEmPautaSemSessao() {
        Long pautaId = 1L;
        Voto voto = new Voto(null, pautaId, "12345678901", EscolhaVoto.SIM);
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pautaService.receberVoto(pautaId, voto));

        assertEquals("Sessão não encontrada para esta pauta", exception.getMessage());
        verify(votoRepository, never()).salvar(any());
    }

    // 4. Obter Resultado
    @Test
    public void deveCalcularResultadoComVitoriaSim() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        List<Voto> votos = List.of(
                new Voto(1L, pautaId, "A1", EscolhaVoto.SIM),
                new Voto(2L, pautaId, "A2", EscolhaVoto.SIM),
                new Voto(3L, pautaId, "A3", EscolhaVoto.NAO));

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(votoRepository.buscarVotosPorPauta(pautaId)).thenReturn(votos);

        ResultadoPauta resultado = pautaService.obterResultado(pautaId);

        assertEquals(2L, resultado.totalSim());
        assertEquals(1L, resultado.totalNao());
        assertEquals(3L, resultado.totalVotos());
        assertEquals("SIM", resultado.resultado());
    }

    @Test
    @DisplayName("Deve falhar quando o CPF tem mais de 11 caracteres")
    public void deveFalharCpfComTamanhoInvalido() {
        // Cenário: CPF com 14 dígitos (causa erro de VARCHAR no banco)
        Voto votoLongo = new Voto(null, 1L, "12345678901234", EscolhaVoto.SIM);
        assertThrows(RuntimeException.class, () -> pautaService.receberVoto(1L, votoLongo));
    }

    @Test
    @DisplayName("Deve falhar quando o CPF contém letras")
    public void deveFalharCpfComLetras() {
        Long pautaId = 1L;
        Voto votoComLetras = new Voto(null, pautaId, "12345678e01", EscolhaVoto.SIM);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pautaService.receberVoto(pautaId, votoComLetras));

        assertEquals("CPF deve conter apenas números", ex.getMessage());

        verify(cpfValidationPort, never()).isAbleToVote(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o integrador externo retorna 404 (Inexistente)")
    public void deveLancarExcecaoQuandoCpfNaoExisteNaBaseExterna() {
        // GIVEN
        Long pautaId = 1L;
        String cpf = "00000000000";
        Voto voto = new Voto(null, pautaId, cpf, EscolhaVoto.NAO);
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoAberta()));
        when(cpfValidationPort.isAbleToVote(cpf))
                .thenThrow(new ResourceNotFoundException("CPF inválido ou não encontrado no sistema de validação."));
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> pautaService.receberVoto(pautaId, voto));

        assertEquals("CPF inválido ou não encontrado no sistema de validação.", ex.getMessage());
        verify(votoRepository, never()).salvar(any());
    }

    @Test
    public void deveCalcularResultadoComEmpate() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        List<Voto> votos = List.of(
                new Voto(1L, pautaId, "A1", EscolhaVoto.SIM),
                new Voto(2L, pautaId, "A2", EscolhaVoto.NAO));

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(votoRepository.buscarVotosPorPauta(pautaId)).thenReturn(votos);

        ResultadoPauta resultado = pautaService.obterResultado(pautaId);

        assertEquals(1L, resultado.totalSim());
        assertEquals(1L, resultado.totalNao());
        assertEquals(2L, resultado.totalVotos());
        assertEquals("EMPATE", resultado.resultado());
    }

    @Test
    public void deveCalcularResultadoSemVotos() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(votoRepository.buscarVotosPorPauta(pautaId)).thenReturn(Collections.emptyList());

        ResultadoPauta resultado = pautaService.obterResultado(pautaId);

        assertEquals(0L, resultado.totalSim());
        assertEquals(0L, resultado.totalNao());
        assertEquals(0L, resultado.totalVotos());
        assertEquals("EMPATE", resultado.resultado());
    }

    // 5. Listagem
    @Test
    public void deveListarPautasVazias() {
        when(pautaRepository.listarTodas()).thenReturn(Collections.emptyList());

        List<Pauta> pautas = pautaService.listarPautas();

        assertTrue(pautas.isEmpty());
    }

    @Test
    public void deveListarTodasAsPautas() {
        List<Pauta> mockPautas = List.of(
                new Pauta(1L, "Pauta 1", "Desc 1"),
                new Pauta(2L, "Pauta 2", "Desc 2"));
        when(pautaRepository.listarTodas()).thenReturn(mockPautas);

        List<Pauta> pautas = pautaService.listarPautas();

        assertEquals(2, pautas.size());
    }

    private Sessao sessaoAberta() {
        return new Sessao(
                1L,
                1L,
                LocalDateTime.now().minusMinutes(1), // Iniciou há 1 min
                LocalDateTime.now().plusMinutes(10)  // Termina em 10 min
        );
    }
}
