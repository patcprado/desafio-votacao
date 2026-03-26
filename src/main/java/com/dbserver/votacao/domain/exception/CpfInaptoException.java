package com.dbserver.votacao.domain.exception;

import lombok.Getter;

@Getter
public class CpfInaptoException extends RuntimeException {
    private final String status;

    public CpfInaptoException(String status) {
        super("O associado possui o status: " + status);
        this.status = status;
    }
}