package com.agenda.kanban.reminder.infrastructure.out.persistence;

import com.agenda.kanban.reminder.domain.model.Reminder;
import com.agenda.kanban.reminder.domain.model.Reminder.Channel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

final class ReminderMapper {

    private ReminderMapper() {
    }

    static ReminderJpaEntity toJpaEntity(Reminder reminder) {
        String channels = reminder.getChannels().stream().map(Enum::name).collect(Collectors.joining(","));
        return new ReminderJpaEntity(
                reminder.getId(), reminder.getTaskId(), reminder.getTriggerAt(), reminder.getStatus().name(),
                channels, reminder.getSentAt());
    }

    static Reminder toDomain(ReminderJpaEntity entity) {
        Set<Channel> channels = Arrays.stream(entity.getChannels().split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Channel::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Channel.class)));
        return new Reminder(
                entity.getId(), entity.getTaskId(), entity.getTriggerAt(),
                Reminder.Status.valueOf(entity.getStatus()), channels, entity.getSentAt());
    }
}
