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

-- 4. Procedimiento seguro de activación de usuarios preexistentes (PRD §9.2): vincula credencial y rol
-- a un usuario creado ANTES de la Fase 4 sin asignar contraseñas conocidas o compartidas y sin inventar
-- ningún password predeterminado. El hash BCrypt lo aporta el proceso de activación (proviene de una
-- contraseña elegida por el propio usuario por un canal seguro); la función únicamente VALIDA y ENLAZA.
-- Los usuarios legados siguen sin posibilidad de login hasta que un administrador ejecute esta
-- activación (deny-by-default). Rechaza: hash no BCrypt, rol fuera de la lista, usuario inexistente
-- y reactivación de una cuenta que ya tiene credencial.
CREATE OR REPLACE FUNCTION assign_user_credential(
    p_user_id BIGINT,
    p_password_hash VARCHAR(100),
    p_role VARCHAR(30)
) RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_password_hash NOT LIKE '$2a$%'
       AND p_password_hash NOT LIKE '$2b$%'
       AND p_password_hash NOT LIKE '$2y$%' THEN
        RAISE EXCEPTION 'El hash proporcionado no es BCrypt';
    END IF;

    IF p_role NOT IN ('USER', 'STAFF', 'ADMIN') THEN
        RAISE EXCEPTION 'Rol no permitido: %', p_role;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE id = p_user_id) THEN
        RAISE EXCEPTION 'El usuario % no existe', p_user_id;
    END IF;

    IF EXISTS (SELECT 1 FROM user_credentials WHERE user_id = p_user_id) THEN
        RAISE EXCEPTION 'El usuario % ya tiene credencial asignada', p_user_id;
    END IF;

    INSERT INTO user_credentials (user_id, password_hash) VALUES (p_user_id, p_password_hash);
    INSERT INTO user_roles (user_id, role) VALUES (p_user_id, p_role)
        ON CONFLICT (user_id, role) DO NOTHING;

    RETURN TRUE;
END;
$$;

COMMENT ON FUNCTION assign_user_credential(BIGINT, VARCHAR, VARCHAR) IS
    'Activa de forma segura un usuario preexistente: enlaza hash BCrypt aportado por el proceso de '
    'activacion y su rol inicial, sin passwords por defecto compartidos (PRD capa de seguridad 9.2)';
