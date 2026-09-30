package com.agenda.kanban.note.infrastructure.out.persistence;

import com.agenda.kanban.note.domain.model.DayNote;

final class DayNoteMapper {

    private DayNoteMapper() {
    }

    static DayNoteJpaEntity toJpaEntity(DayNote note) {
        return new DayNoteJpaEntity(
                note.getId(), note.getWorkspaceId(), note.getDate(), note.getContent(), note.getUpdatedAt());
    }

    static DayNote toDomain(DayNoteJpaEntity entity) {
        return new DayNote(
                entity.getId(), entity.getWorkspaceId(), entity.getNoteDate(), entity.getContent(),
                entity.getUpdatedAt());
    }
}
