-- V5: nota de texto libre por reunión de Google Calendar (una por usuario y evento).
CREATE TABLE meeting_notes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    event_id    VARCHAR(512) NOT NULL,
    content     TEXT NOT NULL DEFAULT '',
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_meeting_notes_user_event UNIQUE (user_id, event_id)
);
