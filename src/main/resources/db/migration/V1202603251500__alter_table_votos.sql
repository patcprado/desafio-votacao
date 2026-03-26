ALTER TABLE votos RENAME COLUMN cpf TO associado_id;

CREATE UNIQUE INDEX idx_voto_pauta_associado ON votos (pauta_id, associado_id);

ALTER TABLE votos ADD CONSTRAINT uk_pauta_associado UNIQUE (pauta_id, associado_id);

CREATE INDEX idx_votos_pauta_voto ON votos (pauta_id, voto);