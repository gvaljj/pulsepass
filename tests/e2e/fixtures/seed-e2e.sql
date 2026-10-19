-- ============================================================================
-- PulsePass — Seed E2E (Fase 5, PRD v1.1, §5.1 y §15.1)
-- ----------------------------------------------------------------------------
-- PROPOSITO:
--   Preprovisionar los fixtures NO registrables por HTTP en la BD dedicada de
--   pruebas: venues (incl. el de aforo 1 para TC-BIZ-014C) y los eventos
--   fixture PUB/DRAFT, mas sus asociaciones de artista.
--
-- REGLAS DE USO (NFR-E2E-002 y §23):
--   * Ejecutar SOLO contra la BD dedicada pulsepass_e2e. NUNCA contra dev/prod.
--   * Las migraciones Flyway (V1..V5) deben aplicarse primero (arranque normal
--     de la aplicacion): este seed asume venues/events/artists/user_roles ya
--     existentes como tablas.
--   * Este archivo NO contiene usuarios, contrasenas, hashes BCrypt ni tokens:
--     los actores USER_A/USER_B/STAFF/ADMIN se registran por HTTP y se
--     promueven por SQL con las sentencias de fixtures/README.md.
--   * Antes de ejecutar, sustituye el literal RUNID por tu runId (editor:
--     buscar y reemplazar) o ajusta los codigos a tu corrida. Es idempotente
--     (ON CONFLICT DO NOTHING): puede re-ejecutarse en la misma BD.
--
-- EJEMPLO:
--   psql "$E2E_DB_URL" -f tests/e2e/fixtures/seed-e2e.sql
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. VENUES (E2E-VEN-01 / E2E-VEN-02 y escenario de aforo agotado)
-- ----------------------------------------------------------------------------
-- Venue principal: sirve para todos los flujos de compra (aforo holgado).
INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('VEN-E2E-PRINCIPAL', 'Auditorio PulsePass E2E', 'Bogota', 'Calle Falsa 123', 500, TRUE)
ON CONFLICT (code) DO NOTHING;

-- Venue de aforo 1 (aprobado: variable venueTinyCode). Solo para TC-BIZ-014C:
-- la primera compra lo agota y el evento pasa a SOLD_OUT.
INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('VEN-E2E-AFORO1', 'Sala Minima PulsePass E2E', 'Bogota', 'Calle Falsisima 456', 1, TRUE)
ON CONFLICT (code) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 2. EVENTOS FIXTURE (§5.1: draftEventCode y publishedEventCode)
-- ----------------------------------------------------------------------------
-- Evento PUBLICADO: descubrible por el visitante anonimo (Flujo A) y destino
-- de las compras del Flujo D (aforo 500 via VEN-E2E-PRINCIPAL).
-- event_date lejana (2027) para que no caduque durante la corrida manual.
INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT 'EVT-E2E-RUNID-PUB',
       'PulsePass E2E RUNID - publicado',
       'Fixture E2E Fase 5: evento en PUBLISHED',
       'MUSIC',
       'PUBLISHED',
       TIMESTAMP '2027-12-15 20:00:00',
       0,
       v.id
FROM venues v
WHERE v.code = 'VEN-E2E-PRINCIPAL'
ON CONFLICT (event_code) DO NOTHING;

-- Evento en BORRADOR: debe ser invisible para el anonimo (404 identico al de un
-- inexistente: TC-SEC-004) y visible para STAFF/ADMIN (TC-SEC-015, EVT-02-STAFF).
INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT 'EVT-E2E-RUNID-DRAFT',
       'PulsePass E2E RUNID - borrador',
       'Fixture E2E Fase 5: evento en DRAFT',
       'MUSIC',
       'DRAFT',
       TIMESTAMP '2027-12-16 18:00:00',
       18,
       v.id
FROM venues v
WHERE v.code = 'VEN-E2E-PRINCIPAL'
ON CONFLICT (event_code) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 3. ASOCIACIONES EVENTO-ARTISTA (E2E-EVT-06 y TC-SEC-005)
-- ----------------------------------------------------------------------------
-- Se asocia el artista 'Solar Beat' (migracion V2) a AMBOS eventos fixture:
--   * el anonimo debe ver SOLO el publicado en /api/events/by-artist,
--   * STAFF/ADMIN deben ver ademas el borrador (contraste TC-SEC-005 vs EVT-06-OPS).
-- El id se resuelve por stage_name (no se asumen ids autoincrementales).
INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
JOIN artists a ON a.stage_name = 'Solar Beat'
WHERE e.event_code IN ('EVT-E2E-RUNID-PUB', 'EVT-E2E-RUNID-DRAFT')
ON CONFLICT DO NOTHING;

-- ============================================================================
-- FIN DEL SEED ACTIVO.
-- Las sentencias siguientes son AYUDAS para fixtures/README.md: quedan en
-- comentarios para que el unico efecto del archivo sea el seed de arriba.
-- ============================================================================

-- (A) Promover rol STAFF tras registrar al usuario por HTTP (no usar
--     assign_user_credential: el usuario YA tiene credencial creada por la API
--     y la funcion la rechazaria):
-- INSERT INTO user_roles (user_id, role)
-- SELECT id, 'STAFF' FROM users WHERE username = 'staff-RUNID'
-- ON CONFLICT (user_id, role) DO NOTHING;

-- (B) Promover rol ADMIN tras registrar por HTTP (mismo patron):
-- INSERT INTO user_roles (user_id, role)
-- SELECT id, 'ADMIN' FROM users WHERE username = 'admin-RUNID'
-- ON CONFLICT (user_id, role) DO NOTHING;

-- (C) Desactivar la cuenta temporal para TC-AUTH-013 (DESPUES de capturar el
--     token con TC-AUTH-013B):
-- UPDATE users SET active = FALSE WHERE username = 'inactivo-RUNID';

-- (D) TC-SEC-017 — revocar STAFF, ejecutar la peticion y RESTAURAR:
-- DELETE FROM user_roles
-- WHERE user_id = (SELECT id FROM users WHERE username = 'staff-RUNID')
--   AND role = 'STAFF';
-- ... ejecutar TC-SEC-017 en Postman (esperado 403) ...
-- INSERT INTO user_roles (user_id, role)
-- SELECT id, 'STAFF' FROM users WHERE username = 'staff-RUNID'
-- ON CONFLICT (user_id, role) DO NOTHING;

-- (E) Verificacion alternativa de TC-SEC-016 (sin escalacion de rol):
-- SELECT u.username, r.role
-- FROM users u LEFT JOIN user_roles r ON r.user_id = u.id
-- WHERE u.username = 'admin-fake-RUNID';   -- solo debe devolver role = 'USER'
