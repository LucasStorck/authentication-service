CREATE TABLE roles (
    role_id BIGINT PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL
);

INSERT INTO roles (role_id, name) VALUES (1, 'USER');
INSERT INTO roles (role_id, name) VALUES (2, 'SUPERUSER');