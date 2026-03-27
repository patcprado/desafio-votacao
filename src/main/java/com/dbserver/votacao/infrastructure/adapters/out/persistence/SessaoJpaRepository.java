package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import com.dbserver.votacao.infrastructure.adapters.out.persistence.entity.SessaoEntity; // Importe a Entity
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.QueryHints;
@Repository
public interface SessaoJpaRepository extends JpaRepository<SessaoEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    Optional<SessaoEntity> findFirstByPautaIdOrderByIdDesc(Long pautaId);
}