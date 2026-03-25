package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PautaJpaRepository extends JpaRepository<PautaEntity, Long> {
}