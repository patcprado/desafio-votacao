package com.dbserver.votacao.infrastructure.adapters.in.web.exception;

import com.dbserver.votacao.domain.exception.BusinessException;
import com.dbserver.votacao.domain.exception.CpfInaptoException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleNotFoundException(ResourceNotFoundException ex) {
        log.warn("RECURSO NÃO ENCONTRADO: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDetails("Recurso não encontrado", ex.getMessage(), LocalDateTime.now()), HttpStatus.NOT_FOUND);
    }

    // UNIFICADO: Apenas um método para tratar violação de integridade (Voto Duplicado)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetails> handleConflictException(org.springframework.dao.DataIntegrityViolationException ex) {
        log.warn("CONFLITO DE DADOS (CONSTRAINT): {}", ex.getMessage());

        String titulo = "Erro de Integridade";
        String mensagem = "Operação não permitida: os dados já existem ou violam regras de integridade.";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Regra específica para o Voto Único
        if (ex.getMessage() != null && ex.getMessage().contains("uk_pauta_associado")) {
            titulo = "Conflito de Dados";
            mensagem = "Voto já registrado: este associado já votou nesta pauta.";
            status = HttpStatus.CONFLICT;
        }

        return new ResponseEntity<>(new ErrorDetails(titulo, mensagem, LocalDateTime.now()), status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDetails> handleBusinessException(RuntimeException ex) {
        log.error("REGRA DE NEGÓCIO VIOLADA: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetails("Regra de Negócio Violada", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneralException(Exception ex) {
        log.error("ERRO CRÍTICO NÃO TRATADO: ", ex);
        return new ResponseEntity<>(new ErrorDetails("Erro Interno", "Erro inesperado.", LocalDateTime.now()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ... (Manter handleValidationException e handleHttpMessageNotReadableException como estão)

    public record ErrorDetails(
            @Schema(description = "Título amigável do erro") String titulo,
            @Schema(description = "Detalhes da exceção") String detalhe,

            @Schema(description = "Data e hora do erro") LocalDateTime timestamp) {
    }

    @ExceptionHandler(CpfInaptoException.class)
    public ResponseEntity<Map<String, String>> handleCpfInapto(CpfInaptoException ex) {
        // Retorna exatamente: { "status": "UNABLE_TO_VOTE" }
        // O requisito pede 404 para "invalid/unable" no bônus
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", ex.getStatus()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDetails> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetails("Violação de Regra", ex.getMessage(), LocalDateTime.now()));
    }

}