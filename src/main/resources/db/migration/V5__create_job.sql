CREATE TABLE job (
      id            INTEGER PRIMARY KEY AUTOINCREMENT,
      source        VARCHAR(20) NOT NULL,
      external_id   VARCHAR(50) NOT NULL,
      slug          VARCHAR(255),
      title         VARCHAR(255) NOT NULL,
      company       VARCHAR(255),
      platform      VARCHAR(50),
      url           VARCHAR(1000),
      description   TEXT,
      level         VARCHAR(20),
      area          VARCHAR(20),
      work_mode     VARCHAR(20),
      contract_type VARCHAR(20),
      city          VARCHAR(100),
      state         VARCHAR(100),
      salary_min    INTEGER,
      salary_max    INTEGER,
      published_at  TIMESTAMP,
      active        INTEGER NOT NULL DEFAULT 1,
      synced_at     TIMESTAMP NOT NULL
);

-- A mesma vaga nunca entra duas vezes para a mesma fonte
CREATE UNIQUE INDEX uq_job_source_external_id ON job (source, external_id);

CREATE INDEX idx_job_active ON job (active);

CREATE INDEX idx_job_published_at ON job (published_at);
