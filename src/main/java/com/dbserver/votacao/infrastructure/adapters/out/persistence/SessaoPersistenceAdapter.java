package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import com.dbserver.votacao.application.ports.out.SessaoRepositoryPort;
import com.dbserver.votacao.domain.model.Sessao;
import com.dbserver.votacao.infrastructure.adapters.out.persistence.entity.SessaoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessaoPersistenceAdapter implements SessaoRepositoryPort {

    private final SessaoJpaRepository jpaRepository;

    @Override
    public Sessao salvar(Sessao sessao) {
        // Converte Domínio -> Entidade
        SessaoEntity entity = new SessaoEntity();
        entity.setId(sessao.getId()); // Aqui está a correção vital
        entity.setPautaId(sessao.getPautaId());
        entity.setDataAbertura(sessao.getDataAbertura());
        entity.setDataEncerramento(sessao.getDataEncerramento());

        SessaoEntity salvo = jpaRepository.save(entity);

        // Retorna o domínio com o ID gerado
        sessao.setId(salvo.getId());
        return sessao;
    }

    @Override
    public Optional<Sessao> buscarPorPautaId(Long pautaId) {
        return jpaRepository.findByPautaId(pautaId)
                .map(entity -> new Sessao(
                        entity.getId(),
                        entity.getPautaId(),
                        entity.getDataAbertura(),
                        entity.getDataEncerramento()));
    }

}