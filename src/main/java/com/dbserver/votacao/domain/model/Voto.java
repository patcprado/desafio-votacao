package com.dbserver.votacao.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Voto {
    private Long id;
    private Long pautaId;
    private String associadoId;
    private EscolhaVoto escolha;
}
