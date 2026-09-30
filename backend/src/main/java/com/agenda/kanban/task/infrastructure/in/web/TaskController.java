package com.agenda.kanban.task.infrastructure.in.web;

import com.agenda.kanban.task.application.port.in.CreateTaskUseCase;
import com.agenda.kanban.task.application.port.in.CreateTaskUseCase.CreateTaskCommand;
import com.agenda.kanban.task.application.port.in.CreateTaskUseCase.TaskResult;
import com.agenda.kanban.task.application.port.in.DeleteTaskUseCase;
import com.agenda.kanban.task.application.port.in.DeleteTaskUseCase.DeleteTaskCommand;
import com.agenda.kanban.task.application.port.in.MoveTaskUseCase;
import com.agenda.kanban.task.application.port.in.MoveTaskUseCase.MoveTaskCommand;
import com.agenda.kanban.task.application.port.in.UpdateTaskUseCase;
import com.agenda.kanban.task.application.port.in.UpdateTaskUseCase.UpdateTaskCommand;
import com.agenda.kanban.reminder.application.port.in.SetTaskDueDateUseCase;
import com.agenda.kanban.reminder.application.port.in.SetTaskDueDateUseCase.SetTaskDueDateCommand;
import com.agenda.kanban.reminder.application.port.in.SetTaskDueDateUseCase.TaskDueDateResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para tarjetas/tareas (FR-001). */
@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final MoveTaskUseCase moveTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final SetTaskDueDateUseCase setTaskDueDateUseCase;

    public TaskController(
            CreateTaskUseCase createTaskUseCase,
            UpdateTaskUseCase updateTaskUseCase,
            MoveTaskUseCase moveTaskUseCase,
            DeleteTaskUseCase deleteTaskUseCase,
            SetTaskDueDateUseCase setTaskDueDateUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.moveTaskUseCase = moveTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.setTaskDueDateUseCase = setTaskDueDateUseCase;
    }

    @PostMapping("/boards/columns/{columnId}/tasks")
    public ResponseEntity<TaskResult> createTask(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID columnId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResult result = createTaskUseCase.createTask(
                new CreateTaskCommand(userId, columnId, request.title(), request.description()));
        log.info("Tarea creada: taskId={} columnId={}", result.id(), columnId);
        return ResponseEntity.created(URI.create("/api/v1/tasks/" + result.id())).body(result);
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<Void> updateTask(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        updateTaskUseCase.updateTask(
                new UpdateTaskCommand(userId, taskId, request.title(), request.description()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/tasks/{taskId}/move")
    public ResponseEntity<Void> moveTask(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID taskId,
            @Valid @RequestBody MoveTaskRequest request) {
        moveTaskUseCase.moveTask(new MoveTaskCommand(userId, taskId, request.columnId()));
        log.info("Tarea movida: taskId={} targetColumnId={}", taskId, request.columnId());
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH dedicado para la fecha límite/antelación de recordatorio (FR-003, FR-006, US2-AS1/AS4).
     * Se mantiene como ruta separada de {@code PATCH /tasks/{taskId}} (título/descripción) porque
     * ambos casos de uso viven en módulos hexagonales distintos (task vs. reminder) y así cada
     * adaptador de entrada invoca un único puerto de entrada, sin mezclar responsabilidades.
     */
    @PatchMapping("/tasks/{taskId}/due-date")
    public ResponseEntity<TaskDueDateResponse> setDueDate(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID taskId,
            @Valid @RequestBody SetTaskDueDateRequest request) {
        TaskDueDateResult result = setTaskDueDateUseCase.setDueDate(
                new SetTaskDueDateCommand(userId, taskId, request.dueAt(), request.reminderLeadMinutes()));
        return ResponseEntity.ok(TaskDueDateResponse.from(result));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal UUID userId, @PathVariable UUID taskId) {
        deleteTaskUseCase.deleteTask(new DeleteTaskCommand(userId, taskId));
        return ResponseEntity.noContent().build();
    }

    public record CreateTaskRequest(@NotBlank @Size(min = 1, max = 200) String title, String description) {
    }

    public record UpdateTaskRequest(@NotBlank @Size(min = 1, max = 200) String title, String description) {
    }

    public record MoveTaskRequest(@NotNull UUID columnId) {
    }

    /** {@code dueAt = null} elimina la fecha límite (y cancela el recordatorio pendiente, US2-AS4). */
    public record SetTaskDueDateRequest(Instant dueAt, Integer reminderLeadMinutes) {
    }

    public record TaskDueDateResponse(UUID taskId, Instant dueAt, Integer reminderLeadMinutes, boolean overdue) {
        static TaskDueDateResponse from(TaskDueDateResult result) {
            return new TaskDueDateResponse(
                    result.taskId(), result.dueAt(), result.reminderLeadMinutes(), result.overdue());
        }
    }
}
