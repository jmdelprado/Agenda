package com.agenda.kanban.calendar.infrastructure.out.persistence;

import com.agenda.kanban.calendar.domain.model.MeetingNote;

final class MeetingNoteMapper {

    private MeetingNoteMapper() {
    }

    static MeetingNoteJpaEntity toJpaEntity(MeetingNote note) {
        return new MeetingNoteJpaEntity(
                note.getId(), note.getUserId(), note.getEventId(), note.getContent(), note.getUpdatedAt());
    }

    static MeetingNote toDomain(MeetingNoteJpaEntity entity) {
        return new MeetingNote(
                entity.getId(), entity.getUserId(), entity.getEventId(), entity.getContent(), entity.getUpdatedAt());
    }
}
