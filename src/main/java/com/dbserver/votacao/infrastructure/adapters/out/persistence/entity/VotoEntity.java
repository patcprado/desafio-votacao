package com.dbserver.votacao.infrastructure.adapters.out.persistence.entity;

import com.dbserver.votacao.domain.model.EscolhaVoto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "votos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pautaId;

    @Column(nullable = false)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscolhaVoto escolha;
}
