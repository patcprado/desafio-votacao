package com.dbserver.votacao.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SessaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pautaId;
    private LocalDateTime dataAbertura;
    @Column(name = "data_fechamento")
    private LocalDateTime dataEncerramento;
}