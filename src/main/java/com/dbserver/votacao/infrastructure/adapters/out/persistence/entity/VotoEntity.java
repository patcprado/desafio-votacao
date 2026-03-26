package com.dbserver.votacao.infrastructure.adapters.out.persistence.entity;

import com.dbserver.votacao.domain.model.EscolhaVoto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "votos")
public class VotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pautaId;
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "voto", nullable = false)
    private EscolhaVoto escolha;
}