package com.dbserver.votacao.infrastructure.adapters.out.persistence;

import com.dbserver.votacao.application.ports.out.PautaRepositoryPort;
import com.dbserver.votacao.domain.model.Pauta;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class PautaPersistenceAdapter implements PautaRepositoryPort {

    private final PautaJpaRepository jpaRepository;

    public PautaPersistenceAdapter(PautaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Pauta> listarTodas() {
        return jpaRepository.findAll()
                .stream()
                .map(entity -> {
                    Pauta pauta = new Pauta();
                    pauta.setId(entity.getId());
                    pauta.setTitulo(entity.getTitulo());
                    pauta.setDescricao(entity.getDescricao());
                    return pauta;
                })
                .toList();
    }

    @Override
    public Optional<Pauta> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(entity -> {
                    Pauta pauta = new Pauta();
                    pauta.setId(entity.getId());
                    pauta.setTitulo(entity.getTitulo());
                    pauta.setDescricao(entity.getDescricao());
                    return pauta;
                });
    }

    @Override
    public Pauta salvar(Pauta pauta) {
        // Converte Domínio -> Entidade
        PautaEntity entity = new PautaEntity(
                pauta.getId(),
                pauta.getTitulo(),
                pauta.getDescricao());

        PautaEntity salvo = jpaRepository.save(entity);

        // Converte Entidade -> Domínio (retorno)
        return new Pauta(
                salvo.getId(),
                salvo.getTitulo(),
                salvo.getDescricao());
    }
}
