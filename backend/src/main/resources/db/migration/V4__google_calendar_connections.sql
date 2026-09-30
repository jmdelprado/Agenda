-- V4: conexión OAuth de un usuario con su Google Calendar (una por usuario) — "que aparezcan
-- las reuniones que tengo ese día" en el panel derecho de la Agenda.
CREATE TABLE google_calendar_connections (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    access_token       TEXT NOT NULL,
    refresh_token      TEXT NOT NULL,
    token_expires_at   TIMESTAMPTZ NOT NULL,
    google_email       VARCHAR(255),
    connected_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
