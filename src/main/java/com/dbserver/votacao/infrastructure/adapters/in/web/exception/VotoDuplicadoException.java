package com.dbserver.votacao.infrastructure.adapters.in.web.exception;

public class VotoDuplicadoException extends RuntimeException {
    public VotoDuplicadoException(String cpf, Long pautaId) {
        super(String.format("Voto já registrado para o CPF %s na pauta %d.", cpf, pautaId));
    }

    public VotoDuplicadoException(String message) {
        super(message);
    }
}