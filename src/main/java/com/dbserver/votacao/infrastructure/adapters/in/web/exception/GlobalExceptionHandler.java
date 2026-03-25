package com.dbserver.votacao.infrastructure.adapters.in.web.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice // Melhor que @ControllerAdvice para APIs REST
public class GlobalExceptionHandler {

    @ExceptionHandler(com.dbserver.votacao.infrastructure.adapters.in.web.exception.ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleNotFoundException(Exception ex) {
        log.warn("RECURSO NÃO ENCONTRADO: {}", ex.getMessage());

        ErrorDetails error = new ErrorDetails(
                "Recurso não encontrado",
                ex.getMessage(),
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDetails> handleBusinessException(RuntimeException ex) {
        log.error("REGRA DE NEGÓCIO VIOLADA: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(
                "Regra de Negócio Violada",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneralException(Exception ex) {
        log.error("ERRO CRÍTICO NÃO TRATADO: ", ex);

        ErrorDetails error = new ErrorDetails(
                "Erro Interno no Servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        log.warn("ERRO DE VALIDAÇÃO: {}", ex.getMessage());

        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("A requisição contém dados inválidos ou mal formatados.");

        ErrorDetails error = new ErrorDetails(
                "Erro de Validação",
                mensagem,
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("ERRO DE LEITURA DO JSON: {}", ex.getMessage());

        ErrorDetails error = new ErrorDetails(
                "Corpo da Requisição Inválido",
                "Certifique-se de enviar um JSON válido com os tipos corretos.",
                LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    public record ErrorDetails(
            @Schema(description = "Título amigável do erro", example = "Regra de Negócio Violada") String titulo,

            @Schema(description = "Detalhes da exceção", example = "O associado já votou nesta pauta.") String detalhe,

            @Schema(description = "Data e hora do erro", example = "2026-03-24T22:50:00") LocalDateTime timestamp) {
    }
}