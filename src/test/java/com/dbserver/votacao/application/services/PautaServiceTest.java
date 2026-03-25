package com.dbserver.votacao.application.services;

import com.dbserver.votacao.application.ports.out.PautaRepositoryPort;
import com.dbserver.votacao.application.ports.out.SessaoRepositoryPort;
import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import com.dbserver.votacao.domain.model.EscolhaVoto;
import com.dbserver.votacao.domain.model.Pauta;
import com.dbserver.votacao.domain.model.ResultadoPauta;
import com.dbserver.votacao.domain.model.Sessao;
import com.dbserver.votacao.domain.model.Voto;
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

    // 1. Criar Pauta
    @Test
    void deveCriarPautaComSucesso() {
        Pauta pauta = new Pauta(null, "Título", "Descricao");
        Pauta pautaSalva = new Pauta(1L, "Título", "Descricao");
        when(pautaRepository.salvar(any(Pauta.class))).thenReturn(pautaSalva);

        Pauta resultado = pautaService.criarPauta(pauta);

        assertNotNull(resultado.getId());
        assertEquals("Título", resultado.getTitulo());
        verify(pautaRepository, times(1)).salvar(pauta);
    }

    @Test
    void deveLancarExcecaoAoCriarPautaInvalida() {
        Pauta pauta = new Pauta(null, "", "");
        when(pautaRepository.salvar(any(Pauta.class))).thenThrow(new RuntimeException("Pauta inválida"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pautaService.criarPauta(pauta));

        assertEquals("Pauta inválida", exception.getMessage());
        verify(pautaRepository, times(1)).salvar(pauta);
    }

    // 2. Abrir Sessão
    @Test
    void deveAbrirSessaoComTempoInformado() {
        Long pautaId = 1L;
        Integer minutos = 5;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        
        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.empty());

        pautaService.abrirSessao(pautaId, minutos);

        ArgumentCaptor<Sessao> sessaoCaptor = ArgumentCaptor.forClass(Sessao.class);
        verify(sessaoRepository).salvar(sessaoCaptor.capture());

        Sessao sessaoSalva = sessaoCaptor.getValue();
        assertEquals(pautaId, sessaoSalva.getPautaId());
        assertNotNull(sessaoSalva.getDataAbertura());
        assertNotNull(sessaoSalva.getDataEncerramento());
        assertTrue(sessaoSalva.getDataEncerramento().isAfter(sessaoSalva.getDataAbertura().plusMinutes(minutos - 1)));
    }

    @Test
    void deveAbrirSessaoComTempoDefault() {
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
    void deveLancarExcecaoAoAbrirSessaoParaPautaInexistente() {
        Long pautaId = 99L;
        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pautaService.abrirSessao(pautaId, 10));

        assertEquals("Pauta não encontrada", exception.getMessage());
        verify(sessaoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoAoAbrirSessaoJaAtiva() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        Sessao sessaoAtiva = new Sessao(1L, pautaId, LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        
        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoAtiva));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pautaService.abrirSessao(pautaId, 5));

        assertEquals("Sessão já está aberta para esta pauta", exception.getMessage());
        verify(sessaoRepository, never()).salvar(any());
    }

    // 3. Receber Voto
    @Test
    void deveReceberVotoComSucesso() {
        Long pautaId = 1L;
        Voto voto = new Voto(null, pautaId, "ASSOC-1", EscolhaVoto.SIM);
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        // Sessão ativa
        Sessao sessaoAtiva = new Sessao(1L, pautaId, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(10));

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoAtiva));
        when(votoRepository.existeVotoPorPautaEAssociado(pautaId, "ASSOC-1")).thenReturn(false);

        pautaService.receberVoto(pautaId, voto);

        ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).salvar(votoCaptor.capture());

        assertEquals("ASSOC-1", votoCaptor.getValue().getAssociadoId());
        assertEquals(pautaId, votoCaptor.getValue().getPautaId());
    }

    @Test
    void deveLancarExcecaoAoVotarDuasVezes() {
        Long pautaId = 1L;
        Voto voto = new Voto(null, pautaId, "ASSOC-1", EscolhaVoto.SIM);
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        Sessao sessaoAtiva = new Sessao(1L, pautaId, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(10));

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoAtiva));
        when(votoRepository.existeVotoPorPautaEAssociado(pautaId, "ASSOC-1")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pautaService.receberVoto(pautaId, voto));

        assertEquals("Associado já votou nesta pauta", exception.getMessage());
        verify(votoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoAoVotarEmSessaoExpirada() {
        Long pautaId = 1L;
        Voto voto = new Voto(null, pautaId, "ASSOC-1", EscolhaVoto.SIM);
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        // Sessão expirada
        Sessao sessaoExpirada = new Sessao(1L, pautaId, LocalDateTime.now().minusMinutes(20), LocalDateTime.now().minusMinutes(10));

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.of(sessaoExpirada));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pautaService.receberVoto(pautaId, voto));

        assertEquals("A sessão para esta pauta já está encerrada", exception.getMessage());
        verify(votoRepository, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoAoVotarEmPautaSemSessao() {
        Long pautaId = 1L;
        Voto voto = new Voto(null, pautaId, "ASSOC-1", EscolhaVoto.SIM);
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.buscarPorPautaId(pautaId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pautaService.receberVoto(pautaId, voto));

        assertEquals("Sessão não encontrada para esta pauta", exception.getMessage());
        verify(votoRepository, never()).salvar(any());
    }

    // 4. Obter Resultado
    @Test
    void deveCalcularResultadoComVitoriaSim() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        List<Voto> votos = List.of(
                new Voto(1L, pautaId, "A1", EscolhaVoto.SIM),
                new Voto(2L, pautaId, "A2", EscolhaVoto.SIM),
                new Voto(3L, pautaId, "A3", EscolhaVoto.NAO)
        );

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(votoRepository.buscarVotosPorPauta(pautaId)).thenReturn(votos);

        ResultadoPauta resultado = pautaService.obterResultado(pautaId);

        assertEquals(2, resultado.totalSim());
        assertEquals(1, resultado.totalNao());
        assertEquals(3, resultado.totalVotos());
        assertEquals("SIM", resultado.vencedor());
    }

    @Test
    void deveCalcularResultadoComEmpate() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");
        List<Voto> votos = List.of(
                new Voto(1L, pautaId, "A1", EscolhaVoto.SIM),
                new Voto(2L, pautaId, "A2", EscolhaVoto.NAO)
        );

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(votoRepository.buscarVotosPorPauta(pautaId)).thenReturn(votos);

        ResultadoPauta resultado = pautaService.obterResultado(pautaId);

        assertEquals(1, resultado.totalSim());
        assertEquals(1, resultado.totalNao());
        assertEquals(2, resultado.totalVotos());
        assertEquals("EMPATE", resultado.vencedor());
    }

    @Test
    void deveCalcularResultadoSemVotos() {
        Long pautaId = 1L;
        Pauta pauta = new Pauta(pautaId, "Título", "Desc");

        when(pautaRepository.buscarPorId(pautaId)).thenReturn(Optional.of(pauta));
        when(votoRepository.buscarVotosPorPauta(pautaId)).thenReturn(Collections.emptyList());

        ResultadoPauta resultado = pautaService.obterResultado(pautaId);

        assertEquals(0, resultado.totalSim());
        assertEquals(0, resultado.totalNao());
        assertEquals(0, resultado.totalVotos());
        assertEquals("EMPATE", resultado.vencedor());
    }

    // 5. Listagem
    @Test
    void deveListarPautasVazias() {
        when(pautaRepository.listarTodas()).thenReturn(Collections.emptyList());

        List<Pauta> pautas = pautaService.listarPautas();

        assertTrue(pautas.isEmpty());
    }

    @Test
    void deveListarTodasAsPautas() {
        List<Pauta> mockPautas = List.of(
                new Pauta(1L, "Pauta 1", "Desc 1"),
                new Pauta(2L, "Pauta 2", "Desc 2")
        );
        when(pautaRepository.listarTodas()).thenReturn(mockPautas);

        List<Pauta> pautas = pautaService.listarPautas();

        assertEquals(2, pautas.size());
    }
}
