package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import com.dbserver.votacao.infrastructure.adapters.out.persistence.entity.SessaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessaoJpaRepository extends JpaRepository<SessaoEntity, Long> {
    Optional<SessaoEntity> findFirstByPautaIdOrderByIdDesc(Long pautaId);
}
