package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pautas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PautaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String descricao;
}