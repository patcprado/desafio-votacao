CREATE TABLE pautas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sessoes (
    id BIGSERIAL PRIMARY KEY,
    pauta_id BIGINT NOT NULL REFERENCES pautas(id), -- Ajustado para BIGINT
    data_abertura TIMESTAMP NOT NULL,
    data_fechamento TIMESTAMP NOT NULL
);

CREATE TABLE votos (
    id BIGSERIAL PRIMARY KEY,
    pauta_id BIGINT NOT NULL REFERENCES pautas(id), -- Ajustado para BIGINT
    cpf VARCHAR(11) NOT NULL, -- Garante os 11 caracteres do CPF
    voto VARCHAR(3) NOT NULL, -- 'SIM' ou 'NAO'
    data_voto TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_voto_pauta_cpf UNIQUE (pauta_id, cpf)
);

-- Índice para performance na soma dos resultados
CREATE INDEX idx_votos_pauta ON votos(pauta_id);