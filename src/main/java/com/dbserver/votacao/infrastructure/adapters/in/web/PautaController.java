package com.dbserver.votacao.infrastructure.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dbserver.votacao.application.services.PautaService;
import com.dbserver.votacao.domain.model.Pauta;
import com.dbserver.votacao.domain.model.ResultadoPauta;
import com.dbserver.votacao.domain.service.VotacaoService;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.PautaRequest;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.SessaoResponse;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.VotoRequest;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.VotoResponse;
import com.dbserver.votacao.infrastructure.adapters.in.web.exception.GlobalExceptionHandler;
import com.dbserver.votacao.infrastructure.adapters.in.web.exception.GlobalExceptionHandler.ErrorDetails;
import com.dbserver.votacao.infrastructure.adapters.out.persistence.SessaoPersistenceAdapter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/pautas")
@RequiredArgsConstructor
@Tag(name = "Pautas", description = "Endpoints para gerenciamento de pautas, sessões e votações")
public class PautaController {

        private final PautaService pautaService;
        private final SessaoPersistenceAdapter sessionAdapter;
        private final VotacaoService votacaoService;

        @Operation(summary = "Criar uma nova pauta", description = "Cadastra uma pauta no sistema para posterior votação.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Pauta criada com sucesso", content = @Content(schema = @Schema(implementation = Pauta.class))),
                        @ApiResponse(responseCode = "400", description = "Dados da pauta inválidos", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
        })
        @PostMapping
        public ResponseEntity<Pauta> criar(@RequestBody @Valid PautaRequest request) {
                Pauta novaPauta = new Pauta(null, request.titulo(), request.descricao());
                Pauta pautaSalva = pautaService.criarPauta(novaPauta);
                return ResponseEntity.status(HttpStatus.CREATED).body(pautaSalva);
        }

        @Operation(summary = "Listar todas as pautas")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de pautas retornada com sucesso", content = @Content(schema = @Schema(implementation = Pauta.class)))
        })
        @GetMapping
        public ResponseEntity<List<Pauta>> listar() {
                return ResponseEntity.ok(pautaService.listarPautas());
        }

        @Operation(summary = "Registrar um voto", description = "Recebe o voto (SIM/NAO) de um associado para uma pauta com sessão aberta.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Voto computado com sucesso", content = @Content(schema = @Schema(implementation = VotoResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Regra de negócio violada", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
        })
        @PostMapping("/{id}/votos")
        public ResponseEntity<VotoResponse> votar(
                        @PathVariable Long id,
                        @RequestBody @Valid VotoRequest request) {

                // Agora o serviço retorna os dados do processamento
                VotoResponse response = votacaoService.registrarVoto(id, request.associadoId(), request.escolha());

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Abrir sessão de votação", description = "Inicia cronômetro para votação de uma pauta.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Sessão aberta com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Erro de negócio", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorDetails.class))),
                        @ApiResponse(responseCode = "404", description = "Pauta inexistente", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorDetails.class)))
        })
        @PostMapping("/{pautaId}/sessao") // <-- Sua nova rota escolhida!
        public ResponseEntity<String> abrirSessao(
                        @PathVariable Long pautaId,
                        @RequestParam(defaultValue = "1") Integer minutos) {

                pautaService.abrirSessao(pautaId, minutos);
                return ResponseEntity.ok("Sessão aberta com sucesso por " + minutos + " minutos.");
        }

        @Operation(summary = "Obter resultado da votação")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Resultado obtido com sucesso", content = @Content(schema = @Schema(implementation = ResultadoPauta.class)))
        })
        @GetMapping("/{id}/resultado")
        public ResponseEntity<ResultadoPauta> obterResultado(
                        @Parameter(description = "ID da pauta") @PathVariable Long id) {
                return ResponseEntity.ok(pautaService.obterResultado(id));
        }

        @Operation(summary = "Listar todas as sessões (Debug/Auditoria)", description = "Retorna o histórico de todas as sessões abertas, datas de encerramento e pautas vinculadas.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de sessões obtida com sucesso")
        })
        @GetMapping("/sessoes")
        public ResponseEntity<List<SessaoResponse>> listarSessoes() {
                List<SessaoResponse> response = sessionAdapter.listarTodas().stream()
                                .map(s -> new SessaoResponse(
                                                s.getId(),
                                                s.getPautaId(),
                                                s.getDataAbertura(),
                                                s.getDataEncerramento(),
                                                s.estaAberta()))
                                .toList();
                return ResponseEntity.ok(response);
        }

}