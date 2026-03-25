package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import com.dbserver.votacao.application.ports.out.VotoRepositoryPort;
import com.dbserver.votacao.domain.model.Voto;
import com.dbserver.votacao.infrastructure.adapters.out.persistence.entity.VotoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VotoPersistenceAdapter implements VotoRepositoryPort {

    private final VotoJpaRepository jpaRepository;

    @Override
    public Voto salvar(Voto voto) {
        VotoEntity entity = new VotoEntity();
        entity.setPautaId(voto.getPautaId());

        String cpfNumerico = voto.getAssociadoId().replaceAll("\\D", "");
        entity.setAssociadoId(cpfNumerico);

        entity.setEscolha(voto.getEscolha());

        VotoEntity salvo = jpaRepository.save(entity);
        voto.setId(salvo.getId());
        return voto;
    }

    @Override
    public boolean existeVotoPorPautaEAssociado(Long pautaId, String associadoId) {
        return jpaRepository.existsByPautaIdAndAssociadoId(pautaId, associadoId);
    }

    @Override
    public List<Voto> buscarVotosPorPauta(Long pautaId) {
        return jpaRepository.findByPautaId(pautaId)
                .stream()
                .map(entity -> new Voto(
                        entity.getId(),
                        entity.getPautaId(),
                        entity.getAssociadoId(),
                        entity.getEscolha()
                ))
                .collect(Collectors.toList());
    }
}
