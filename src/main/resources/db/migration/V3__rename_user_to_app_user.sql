ALTER TABLE user RENAME TO app_user;

CREATE UNIQUE INDEX uq_app_user_name ON app_user (name COLLATE NOCASE);
