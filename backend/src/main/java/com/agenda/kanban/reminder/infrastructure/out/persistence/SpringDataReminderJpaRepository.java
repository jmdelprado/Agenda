package com.agenda.kanban.reminder.infrastructure.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataReminderJpaRepository extends JpaRepository<ReminderJpaEntity, UUID> {

    @Query("SELECT r FROM ReminderJpaEntity r WHERE r.taskId = :taskId AND r.status = 'PENDING'")
    Optional<ReminderJpaEntity> findPendingByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT r FROM ReminderJpaEntity r WHERE r.status = 'PENDING' AND r.triggerAt <= :now")
    List<ReminderJpaEntity> findDueForDispatch(@Param("now") Instant now);
}
