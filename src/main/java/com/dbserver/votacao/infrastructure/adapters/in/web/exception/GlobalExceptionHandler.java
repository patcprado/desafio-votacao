package com.dbserver.votacao.infrastructure.adapters.in.web.exception;

import com.dbserver.votacao.domain.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1 & 2: Record com data formatada e tratamento centralizado
    public record ErrorDetails(
            @Schema(description = "Título amigável do erro") String titulo,
            @Schema(description = "Detalhes da exceção") String detalhe,
            @Schema(description = "Data e hora do erro")
            @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime timestamp) {
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleNotFoundException(ResourceNotFoundException ex) {
        log.warn("RECURSO NÃO ENCONTRADO: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDetails("Recurso não encontrado", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler({BusinessException.class, VotoDuplicadoException.class})
    public ResponseEntity<ErrorDetails> handleBusinessErrors(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String titulo = "Violação de Regra";

        if (ex instanceof VotoDuplicadoException) {
            log.warn("CONFLITO DE VOTO: {}", ex.getMessage());
            status = HttpStatus.CONFLICT;
            titulo = "Conflito de Registro";
        } else {
            log.error("ERRO DE NEGÓCIO: {}", ex.getMessage());
        }

        return ResponseEntity.status(status)
                .body(new ErrorDetails(titulo, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        String mensagemUnificada = String.join(" , ", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetails("Dados Inválidos", mensagemUnificada, LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneralException(Exception ex) {
        log.error("ERRO CRÍTICO: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDetails("Erro Interno", "Ocorreu um erro inesperado no servidor.", LocalDateTime.now()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Garante o 404 no nível do protocolo
    public ResponseEntity<ErrorDetails> handleNoHandlerFound(NoHandlerFoundException ex) {
        ErrorDetails error = new ErrorDetails(
                "Recurso não encontrado",
                "A rota " + ex.getRequestURL() + " não foi encontrada em nossa API.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
