package com.dbserver.votacao.infrastructure.adapters.in.web;

import com.dbserver.votacao.application.services.PautaService;
import com.dbserver.votacao.domain.model.Pauta;
import com.dbserver.votacao.infrastructure.adapters.in.web.dto.PautaRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.dbserver.votacao.infrastructure.adapters.in.web.dto.VotoRequest;
import com.dbserver.votacao.domain.model.Voto;
import com.dbserver.votacao.domain.model.ResultadoPauta;

@RestController
@RequestMapping("/v1/pautas")
@RequiredArgsConstructor
public class PautaController {

    private final PautaService pautaService;

    @PostMapping
    public ResponseEntity<Pauta> criar(@RequestBody @Valid PautaRequest request) {
        // Converte DTO para Modelo de Domínio
        Pauta novaPauta = new Pauta(null, request.titulo(), request.descricao());

        Pauta pautaSalva = pautaService.criarPauta(novaPauta);
        return ResponseEntity.status(HttpStatus.CREATED).body(pautaSalva);
    }

    @GetMapping
    public ResponseEntity<List<Pauta>> listar() {
        return ResponseEntity.ok(pautaService.listarPautas());
    }

    @PostMapping("/{id}/votos")
    public ResponseEntity<Void> votar(@PathVariable Long id, @RequestBody @Valid VotoRequest request) {
        Voto voto = new Voto();
        voto.setAssociadoId(request.associadoId());
        voto.setEscolha(request.escolha());

        pautaService.receberVoto(id, voto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/abrir")
    public ResponseEntity<String> abrirSessao(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer minutos) {

        pautaService.abrirSessao(id, minutos);
        return ResponseEntity.ok("Sessão aberta com sucesso por " + minutos + " minutos.");
    }

    @GetMapping("/{id}/resultado")
    public ResponseEntity<ResultadoPauta> obterResultado(@PathVariable Long id) {
        return ResponseEntity.ok(pautaService.obterResultado(id));
    }
}