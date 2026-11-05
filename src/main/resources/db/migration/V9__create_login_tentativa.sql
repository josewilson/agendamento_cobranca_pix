-- Contador de falhas de login por email, compartilhado entre replicas da aplicacao.
CREATE TABLE login_tentativa (
    email VARCHAR(255) PRIMARY KEY,
    falhas INTEGER NOT NULL,
    bloqueado_ate TIMESTAMPTZ
);
