package com.dbserver.votacao.application.ports.out;

import com.dbserver.votacao.domain.model.Pauta;
import java.util.List;
import java.util.Optional;

public interface PautaRepositoryPort {
    Pauta salvar(Pauta pauta);

    Optional<Pauta> buscarPorId(Long id);

    List<Pauta> listarTodas();
}