package com.dbserver.votacao.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessoes")
@Data
public class SessaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pautaId;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;
}