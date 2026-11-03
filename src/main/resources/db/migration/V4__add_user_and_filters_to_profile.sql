-- A tabela profile ainda não tinha dados que valessem a pena guardar: é apagada e recriada
-- com o dono (user_id) e os filtros opcionais. Os enums ficam como texto (nome do valor).
DROP TABLE profile;

CREATE TABLE profile (
      id            INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id       INTEGER NOT NULL REFERENCES app_user (id),
      name          VARCHAR(100) NOT NULL,
      level         VARCHAR(20),
      area          VARCHAR(20),
      work_mode     VARCHAR(20),
      contract_type VARCHAR(20),
      city          VARCHAR(100),
      state         VARCHAR(100),
      min_salary    INTEGER,
      created_at    TIMESTAMP NOT NULL,
      updated_at    TIMESTAMP NOT NULL
);

-- Nome único por usuário, sem diferenciar maiúsculas (igual ao app_user); o índice também
-- serve para listar os perfis de um usuário.
CREATE UNIQUE INDEX uq_profile_user_id_name ON profile (user_id, name COLLATE NOCASE);
