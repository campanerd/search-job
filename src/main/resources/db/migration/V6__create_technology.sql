CREATE TABLE technology (
      id   INTEGER PRIMARY KEY AUTOINCREMENT,
      name VARCHAR(100) NOT NULL
);

-- Nome único sem diferenciar maiúsculas (igual ao app_user): "Java" e "java" são a mesma tecnologia
CREATE UNIQUE INDEX uq_technology_name ON technology (name COLLATE NOCASE);

CREATE TABLE job_technology (
      job_id        INTEGER NOT NULL REFERENCES job (id),
      technology_id INTEGER NOT NULL REFERENCES technology (id),
      PRIMARY KEY (job_id, technology_id)
);

-- A chave primária já serve para listar as tecnologias de uma vaga; este índice serve para
-- achar as vagas de uma tecnologia.
CREATE INDEX idx_job_technology_technology_id ON job_technology (technology_id);
