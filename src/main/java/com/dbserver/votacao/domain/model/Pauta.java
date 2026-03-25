package com.dbserver.votacao.domain.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pauta {
    private Long id;
    private String titulo;
    private String descricao;
}