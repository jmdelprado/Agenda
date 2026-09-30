package com.agenda.kanban.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita los jobs {@code @Scheduled} (p.ej. ReminderSchedulerAdapter en la User Story 2). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
