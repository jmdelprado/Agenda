-- V1: esquema inicial (usuarios, espacios de trabajo, tableros, columnas, tareas, recordatorios, notificaciones)
-- Ver specs/001-kanban-agenda-workspaces/data-model.md para el detalle de cada entidad y sus reglas.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- User: cuenta individual (FR-013).
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- Workspace: espacio de trabajo / pestaña (FR-008, FR-009).
CREATE TABLE workspaces (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    archived_at     TIMESTAMPTZ NULL,
    CONSTRAINT uq_workspaces_user_name UNIQUE (user_id, name),
    CONSTRAINT chk_workspaces_name_not_blank CHECK (length(btrim(name)) BETWEEN 1 AND 100)
);
CREATE INDEX idx_workspaces_user_id ON workspaces (user_id);

-- Board: tablero 1:1 con Workspace.
CREATE TABLE boards (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id    UUID NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    CONSTRAINT uq_boards_workspace UNIQUE (workspace_id)
);

-- Column (FR-002): tabla nombrada "board_columns" para evitar ambigüedad con la palabra reservada "column".
CREATE TABLE board_columns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    board_id        UUID NOT NULL REFERENCES boards (id) ON DELETE CASCADE,
    name            VARCHAR(50) NOT NULL,
    position        INTEGER NOT NULL,
    CONSTRAINT uq_board_columns_board_position UNIQUE (board_id, position),
    CONSTRAINT chk_board_columns_name_not_blank CHECK (length(btrim(name)) BETWEEN 1 AND 50)
);
CREATE INDEX idx_board_columns_board_id ON board_columns (board_id);

-- Task / Card (FR-001, FR-003, FR-006, FR-007).
CREATE TABLE tasks (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    column_id               UUID NOT NULL REFERENCES board_columns (id) ON DELETE CASCADE,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT NULL,
    due_at                  TIMESTAMPTZ NULL,
    reminder_lead_minutes   INTEGER NULL,
    completed_at            TIMESTAMPTZ NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_tasks_title_not_blank CHECK (length(btrim(title)) BETWEEN 1 AND 200),
    CONSTRAINT chk_tasks_reminder_lead_minutes_positive CHECK (reminder_lead_minutes IS NULL OR reminder_lead_minutes >= 0)
);
CREATE INDEX idx_tasks_column_id ON tasks (column_id);
CREATE INDEX idx_tasks_due_at ON tasks (due_at);

-- Reminder (FR-005, FR-006, FR-014).
CREATE TABLE reminders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    trigger_at      TIMESTAMPTZ NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    channels        VARCHAR(50) NOT NULL DEFAULT 'IN_APP,EMAIL',
    sent_at         TIMESTAMPTZ NULL,
    CONSTRAINT chk_reminders_status CHECK (status IN ('PENDING', 'SENT', 'CANCELLED', 'FAILED'))
);
CREATE INDEX idx_reminders_task_id ON reminders (task_id);
CREATE INDEX idx_reminders_status_trigger_at ON reminders (status, trigger_at);

-- Notification (FR-014): registro de lo mostrado en el panel in-app.
CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reminder_id     UUID NOT NULL REFERENCES reminders (id) ON DELETE CASCADE,
    message         TEXT NOT NULL,
    read_at         TIMESTAMPTZ NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
