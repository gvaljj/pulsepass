-- V6 (PulsePass frontend seed): sedes, eventos publicados y asociaciones de artistas.
-- Migracion incremental: no modifica V1..V5.

INSERT INTO venues (code, name, city, address, capacity, active) VALUES
    ('TEATRO-MAIN', 'Teatro Principal', 'Cartagena', 'Centro Histórico', 500, true),
    ('ARENA-CENTRO', 'Arena Centro', 'Santa Marta', 'Zona Norte', 1200, true),
    ('SALA-PEQUENA', 'Sala Pequeña', 'Barranquilla', 'Centro', 150, true),
    ('CONVENCION-MARINA', 'Marina Convention Center', 'Santa Marta', 'Zona Portuaria', 800, true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO events (
    event_code,
    name,
    description,
    category,
    status,
    event_date,
    minimum_age,
    venue_id
)
VALUES
(
    'JAZZ-2026',
    'Noche de Jazz y Cultura',
    'Tres noches de música caribeña frente a la bahía.',
    'MUSIC',
    'PUBLISHED',
    TIMESTAMP '2026-12-12 20:00:00',
    18,
    (SELECT id FROM venues WHERE code = 'TEATRO-MAIN')
),
(
    'TECH-FEST-2026',
    'Festival de Tecnología',
    'Conferencias, workshops y networking para la comunidad tech.',
    'TECHNOLOGY',
    'PUBLISHED',
    TIMESTAMP '2026-12-20 09:00:00',
    0,
    (SELECT id FROM venues WHERE code = 'ARENA-CENTRO')
),
(
    'CARIBBEAN-FEST-2026',
    'Caribbean Music Fest 2026',
    'Festival de música caribeña con artistas invitados.',
    'ENTERTAINMENT',
    'PUBLISHED',
    TIMESTAMP '2026-12-04 19:30:00',
    16,
    (SELECT id FROM venues WHERE code = 'CONVENCION-MARINA')
)
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
JOIN artists a ON a.stage_name IN ('Caribbean Sound', 'Ocean Drive')
WHERE e.event_code = 'JAZZ-2026'
ON CONFLICT (event_id, artist_id) DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
JOIN artists a ON a.stage_name IN ('Digital Pulse')
WHERE e.event_code = 'TECH-FEST-2026'
ON CONFLICT (event_id, artist_id) DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
JOIN artists a ON a.stage_name IN ('Neon Waves', 'Ocean Drive')
WHERE e.event_code = 'CARIBBEAN-FEST-2026'
ON CONFLICT (event_id, artist_id) DO NOTHING;