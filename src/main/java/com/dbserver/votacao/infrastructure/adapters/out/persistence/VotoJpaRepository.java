package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import com.dbserver.votacao.infrastructure.adapters.out.persistence.entity.VotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotoJpaRepository extends JpaRepository<VotoEntity, Long> {
    boolean existsByPautaIdAndAssociadoId(Long pautaId, String associadoId);
    List<VotoEntity> findByPautaId(Long pautaId);
}
