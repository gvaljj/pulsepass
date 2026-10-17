-- V5 (PRD Capa de Seguridad §9.1): credenciales BCrypt y roles por usuario.
-- Migración incremental: NO modifica ni datos ni estructura de V1..V4.
-- Los usuarios legados (creados antes de la Fase 4) quedan sin credenciales y sin roles:
-- no podrán autenticarse hasta que se les asigne contraseña/rol (deny-by-default en la capa de seguridad).

-- 1. Tabla USER_CREDENTIALS (1:1 con users). password_hash guarda únicamente el hash BCrypt, nunca la contraseña en claro.
CREATE TABLE user_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Tabla USER_ROLES (N:1 con users). PK compuesta para impedir roles duplicados por usuario.
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_role_value CHECK (role IN ('USER', 'STAFF', 'ADMIN'))
);

-- 3. Índices auxiliares (búsquedas frecuentes del CustomUserDetailsService)
CREATE INDEX idx_user_credentials_user_id ON user_credentials(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);
