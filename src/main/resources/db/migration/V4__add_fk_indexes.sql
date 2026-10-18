-- V4: Indices sobre columnas foraneas consultadas frecuentemente.
-- PostgreSQL no auto-indexa columnas referenciadas por FOREIGN KEY.
-- Se versiona como migracion incremental (V1 no se modifica).
CREATE INDEX idx_events_venue_id ON events (venue_id);
CREATE INDEX idx_user_profiles_user_id ON user_profiles (user_id);
CREATE INDEX idx_tickets_user_id ON tickets (user_id);
CREATE INDEX idx_tickets_event_id ON tickets (event_id);
