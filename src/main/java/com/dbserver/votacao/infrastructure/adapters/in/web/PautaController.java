package com.dbserver.votacao.infrastructure.adapters.in.web;

import com.dbserver.votacao.application.services.PautaService;
import com.dbserver.votacao.domain.model.Pauta;
import com.dbserver.votacao.domain.model.ResultadoPauta;
import com.dbserver.votacao.domain.model.Voto;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.PautaRequest;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.VotoRequest;
import com.dbserver.votacao.infrastructure.adapters.in.web.exception.GlobalExceptionHandler.ErrorDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/pautas")
@RequiredArgsConstructor
@Tag(name = "Pautas", description = "Endpoints para gerenciamento de pautas, sessões e votações")
public class PautaController {

    private final PautaService pautaService;

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
            @ApiResponse(responseCode = "201", description = "Voto computado com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Regra de negócio violada", content = @Content(schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "Pauta inexistente", content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    @PostMapping("/{id}/votos")
    public ResponseEntity<Void> votar(
            @Parameter(description = "ID da pauta") @PathVariable Long id,
            @RequestBody @Valid VotoRequest request) {

        Voto voto = new Voto();
        voto.setAssociadoId(request.associadoId());
        voto.setEscolha(com.dbserver.votacao.domain.model.EscolhaVoto.valueOf(request.escolha().toUpperCase()));

        pautaService.receberVoto(id, voto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Abrir sessão de votação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessão aberta com sucesso", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/{id}/abrir")
    public ResponseEntity<String> abrirSessao(
            @Parameter(description = "ID da pauta") @PathVariable Long id,
            @Parameter(description = "Duração em minutos (padrão é 1)") @RequestParam(defaultValue = "1") Integer minutos) {

        pautaService.abrirSessao(id, minutos);
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
}