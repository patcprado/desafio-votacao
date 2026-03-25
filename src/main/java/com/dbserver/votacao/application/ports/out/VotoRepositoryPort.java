package com.dbserver.votacao.application.ports.out;

import com.dbserver.votacao.domain.model.Voto;
import java.util.List;

public interface VotoRepositoryPort {
    Voto salvar(Voto voto);
    boolean existeVotoPorPautaEAssociado(Long pautaId, String associadoId);
    List<Voto> buscarVotosPorPauta(Long pautaId);
}
