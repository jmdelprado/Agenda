package com.agenda.kanban.assistant.application.service;

import com.agenda.kanban.assistant.application.port.in.GenerateFromMeetingNoteUseCase;
import com.agenda.kanban.assistant.application.port.out.AiExtractionClientPort;
import com.agenda.kanban.assistant.application.port.out.AiExtractionClientPort.ExtractionResult;
import com.agenda.kanban.board.application.port.out.BoardRepositoryPort;
import com.agenda.kanban.board.application.port.out.ColumnRepositoryPort;
import com.agenda.kanban.board.domain.model.Column;
import com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase;
import com.agenda.kanban.calendar.application.port.in.GetMeetingNotesUseCase.MeetingNoteResult;
import com.agenda.kanban.note.application.port.in.CreateChecklistItemUseCase;
import com.agenda.kanban.note.application.port.in.CreateChecklistItemUseCase.CreateChecklistItemCommand;
import com.agenda.kanban.note.application.port.in.GetDayNotesUseCase;
import com.agenda.kanban.note.application.port.in.GetDayNotesUseCase.DayNoteResult;
import com.agenda.kanban.note.application.port.in.SaveDayNoteUseCase;
import com.agenda.kanban.note.application.port.in.SaveDayNoteUseCase.SaveDayNoteCommand;
import com.agenda.kanban.shared.ForbiddenOperationException;
import com.agenda.kanban.shared.NotFoundException;
import com.agenda.kanban.shared.ValidationException;
import com.agenda.kanban.task.application.port.in.CreateTaskUseCase;
import com.agenda.kanban.task.application.port.in.CreateTaskUseCase.CreateTaskCommand;
import com.agenda.kanban.workspace.application.port.out.WorkspaceRepositoryPort;
import com.agenda.kanban.workspace.domain.model.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ver {@link GenerateFromMeetingNoteUseCase}. Las tareas se crean en la primera columna (por
 * posición) del tablero del espacio de trabajo indicado — la columna de entrada, nunca la
 * terminal. El checklist y el resumen se apuntan en la fecha indicada (normalmente el día de la
 * reunión).
 */
@Service
public class GenerateFromMeetingNoteService implements GenerateFromMeetingNoteUseCase {

    private final AiExtractionClientPort aiExtractionClient;
    private final GetMeetingNotesUseCase getMeetingNotesUseCase;
    private final WorkspaceRepositoryPort workspaceRepository;
    private final BoardRepositoryPort boardRepository;
    private final ColumnRepositoryPort columnRepository;
    private final CreateTaskUseCase createTaskUseCase;
    private final CreateChecklistItemUseCase createChecklistItemUseCase;
    private final GetDayNotesUseCase getDayNotesUseCase;
    private final SaveDayNoteUseCase saveDayNoteUseCase;

    public GenerateFromMeetingNoteService(
            AiExtractionClientPort aiExtractionClient,
            GetMeetingNotesUseCase getMeetingNotesUseCase,
            WorkspaceRepositoryPort workspaceRepository,
            BoardRepositoryPort boardRepository,
            ColumnRepositoryPort columnRepository,
            CreateTaskUseCase createTaskUseCase,
            CreateChecklistItemUseCase createChecklistItemUseCase,
            GetDayNotesUseCase getDayNotesUseCase,
            SaveDayNoteUseCase saveDayNoteUseCase) {
        this.aiExtractionClient = aiExtractionClient;
        this.getMeetingNotesUseCase = getMeetingNotesUseCase;
        this.workspaceRepository = workspaceRepository;
        this.boardRepository = boardRepository;
        this.columnRepository = columnRepository;
        this.createTaskUseCase = createTaskUseCase;
        this.createChecklistItemUseCase = createChecklistItemUseCase;
        this.getDayNotesUseCase = getDayNotesUseCase;
        this.saveDayNoteUseCase = saveDayNoteUseCase;
    }

    @Override
    @Transactional
    public GenerateResult generate(GenerateCommand command) {
        Workspace workspace = workspaceRepository.findById(command.workspaceId())
                .orElseThrow(() -> new NotFoundException("Espacio de trabajo no encontrado"));
        if (!workspace.belongsTo(command.userId())) {
            throw new ForbiddenOperationException("El espacio de trabajo no pertenece al usuario autenticado");
        }

        String noteContent = getMeetingNotesUseCase.getNotes(command.userId()).stream()
                .filter(note -> note.eventId().equals(command.eventId()))
                .findFirst()
                .map(MeetingNoteResult::content)
                .orElse("");
        if (noteContent.isBlank()) {
            throw new ValidationException("Escribe una nota sobre la reunión antes de generar con IA");
        }

        ExtractionResult extraction = aiExtractionClient.extract(command.meetingTitle(), noteContent);

        int tasksCreated = createTasks(command, extraction.tasks());
        int checklistCreated = createChecklistItems(command, extraction.checklistItems());
        boolean noteUpdated = appendSummaryToDayNote(command, extraction.summary());

        return new GenerateResult(tasksCreated, checklistCreated, noteUpdated);
    }

    private int createTasks(GenerateCommand command, List<String> titles) {
        if (titles.isEmpty()) {
            return 0;
        }
        Optional<UUID> firstColumnId = resolveFirstColumnId(command.workspaceId());
        if (firstColumnId.isEmpty()) {
            return 0;
        }
        for (String title : titles) {
            createTaskUseCase.createTask(new CreateTaskCommand(command.userId(), firstColumnId.get(), title, null));
        }
        return titles.size();
    }

    private Optional<UUID> resolveFirstColumnId(UUID workspaceId) {
        return boardRepository.findByWorkspaceId(workspaceId)
                .flatMap(board -> columnRepository.findByBoardIdOrderByPosition(board.getId()).stream().findFirst())
                .map(Column::getId);
    }

    private int createChecklistItems(GenerateCommand command, List<String> texts) {
        for (String text : texts) {
            createChecklistItemUseCase.create(
                    new CreateChecklistItemCommand(command.userId(), command.workspaceId(), command.date(), text));
        }
        return texts.size();
    }

    private boolean appendSummaryToDayNote(GenerateCommand command, String summary) {
        if (summary == null || summary.isBlank()) {
            return false;
        }
        String existing = getDayNotesUseCase.getNotes(command.userId(), command.workspaceId()).stream()
                .filter(note -> note.date().equals(command.date()))
                .findFirst()
                .map(DayNoteResult::content)
                .orElse("");
        String updated = existing.isBlank() ? summary : existing + "\n\n" + summary;
        saveDayNoteUseCase.save(new SaveDayNoteCommand(command.userId(), command.workspaceId(), command.date(), updated));
        return true;
    }
}
