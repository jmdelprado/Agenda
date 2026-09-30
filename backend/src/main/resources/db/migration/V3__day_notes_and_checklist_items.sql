-- V3: notas de texto libre y checklist por día en la Agenda de papel (independiente de las
-- tarjetas Kanban) — "dejar escribir en la agenda, apuntar checklist y demás".

-- DayNote: una nota de texto libre por (workspace_id, note_date).
CREATE TABLE day_notes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id    UUID NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    note_date       DATE NOT NULL,
    content         TEXT NOT NULL DEFAULT '',
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_day_notes_workspace_date UNIQUE (workspace_id, note_date)
);

-- ChecklistItem: varios apuntes rápidos por (workspace_id, item_date), independientes de "tasks".
CREATE TABLE checklist_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id    UUID NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    item_date       DATE NOT NULL,
    text            VARCHAR(500) NOT NULL,
    done            BOOLEAN NOT NULL DEFAULT false,
    position        INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_checklist_items_text_not_blank CHECK (length(btrim(text)) BETWEEN 1 AND 500)
);
CREATE INDEX idx_checklist_items_workspace_id_item_date ON checklist_items (workspace_id, item_date);
