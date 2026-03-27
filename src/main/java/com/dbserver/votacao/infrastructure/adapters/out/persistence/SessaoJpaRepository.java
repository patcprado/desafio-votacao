package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import com.dbserver.votacao.infrastructure.adapters.out.persistence.entity.SessaoEntity; // Importe a Entity
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SessaoJpaRepository extends JpaRepository<SessaoEntity, Long> { // Use SessaoEntity aqui

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SessaoEntity> findFirstByPautaIdOrderByIdDesc(Long pautaId); // E aqui
}