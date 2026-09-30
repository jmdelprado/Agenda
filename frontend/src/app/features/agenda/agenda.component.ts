import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, map } from 'rxjs';

import { AgendaService, AgendaTaskView } from './agenda.service';
import { ChecklistItemView, DayNoteView, NotesChecklistService } from './notes-checklist.service';
import {
  CalendarEventView,
  CalendarStatus,
  GenerateFromMeetingResult,
  GoogleCalendarService,
} from './google-calendar.service';
import { WorkspaceService, WorkspaceSummary } from '../workspaces/workspace.service';
import { ActiveWorkspaceStore } from '../../core/state/active-workspace.store';
import { MonthGrid, addDays, buildMonthGrid, dateKey, parseDateKey, startOfDay, workspaceTabColor } from './date-utils';

interface StripDay {
  date: Date;
  iso: string;
  hasTasks: boolean;
}

interface PendingTaskEntry {
  task: AgendaTaskView;
  workspaceId: string;
  workspaceName: string;
}

const NOTE_SAVE_DEBOUNCE_MS = 600;

/**
 * Agenda del espacio de trabajo activo (User Story 2, T060): se presenta como una libreta de
 * papel con una página por día. Cada página lista las tareas con fecha límite de ese día
 * (FR-004, FR-007), permite escribir una nota libre y apuntar una checklist rápida
 * (independiente de las tarjetas Kanban). Un carril de meses a la izquierda permite saltar de
 * mes y un panel de pendientes a la derecha consolida todo lo no completado. Desde User Story 3
 * (T068) se recarga automáticamente al cambiar de espacio de trabajo (pestaña).
 */
@Component({
  selector: 'app-agenda',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agenda.component.html',
  styleUrl: './agenda.component.css',
})
export class AgendaComponent {
  private readonly agendaService = inject(AgendaService);
  private readonly notesChecklistService = inject(NotesChecklistService);
  private readonly googleCalendarService = inject(GoogleCalendarService);
  private readonly workspaceService = inject(WorkspaceService);
  private readonly activeWorkspaceStore = inject(ActiveWorkspaceStore);
  private readonly route = inject(ActivatedRoute);

  readonly tasks = signal<AgendaTaskView[]>([]);
  readonly notes = signal<DayNoteView[]>([]);
  readonly checklist = signal<ChecklistItemView[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly selectedDate = signal<Date>(startOfDay(new Date()));
  readonly selectedIso = computed(() => dateKey(this.selectedDate()));
  readonly todayIso = dateKey(startOfDay(new Date()));
  readonly isToday = computed(() => this.selectedIso() === this.todayIso);

  readonly noteDraft = signal('');
  readonly newChecklistText = signal('');
  private noteSaveTimer: ReturnType<typeof setTimeout> | null = null;

  readonly calendarStatus = signal<CalendarStatus | null>(null);
  readonly calendarEvents = signal<CalendarEventView[]>([]);
  readonly calendarLoading = signal(false);
  readonly calendarErrorMessage = signal<string | null>(null);

  readonly meetingNoteDrafts = signal<Record<string, string>>({});
  private meetingNoteSaveTimers: Record<string, ReturnType<typeof setTimeout>> = {};
  readonly activeMeetingNote = signal<CalendarEventView | null>(null);

  readonly generatingEventId = signal<string | null>(null);
  readonly generateMessageByEvent = signal<Record<string, string>>({});

  readonly workspaces = signal<WorkspaceSummary[]>([]);
  readonly activeWorkspaceName = computed(
    () => this.workspaces().find((w) => w.id === this.activeWorkspaceStore.activeWorkspaceId())?.name ?? '',
  );

  readonly pendingTaskEntries = signal<PendingTaskEntry[]>([]);

  readonly tasksByDay = computed(() => {
    const map = new Map<string, AgendaTaskView[]>();
    for (const task of this.tasks()) {
      const key = dateKey(new Date(task.dueAt));
      const list = map.get(key) ?? [];
      list.push(task);
      map.set(key, list);
    }
    for (const list of map.values()) {
      list.sort((a, b) => a.dueAt.localeCompare(b.dueAt));
    }
    return map;
  });

  readonly notesByDay = computed(() => {
    const map = new Map<string, string>();
    for (const note of this.notes()) {
      map.set(note.date, note.content);
    }
    return map;
  });

  readonly checklistByDay = computed(() => {
    const map = new Map<string, ChecklistItemView[]>();
    for (const item of this.checklist()) {
      const list = map.get(item.date) ?? [];
      list.push(item);
      map.set(item.date, list);
    }
    for (const list of map.values()) {
      list.sort((a, b) => a.position - b.position);
    }
    return map;
  });

  readonly tasksForSelectedDay = computed(() => this.tasksByDay().get(this.selectedIso()) ?? []);
  readonly checklistForSelectedDay = computed(() => this.checklistByDay().get(this.selectedIso()) ?? []);

  readonly weekStrip = computed<StripDay[]>(() => {
    const byDay = this.tasksByDay();
    const base = this.selectedDate();
    const days: StripDay[] = [];
    for (let offset = -3; offset <= 3; offset += 1) {
      const date = addDays(base, offset);
      const iso = dateKey(date);
      days.push({ date, iso, hasTasks: byDay.has(iso) });
    }
    return days;
  });

  readonly monthsOfYear = computed<MonthGrid[]>(() => {
    const year = this.selectedDate().getFullYear();
    return Array.from({ length: 12 }, (_, index) => buildMonthGrid(year, index));
  });

  readonly selectedMonthIndex = computed(() => this.selectedDate().getMonth());

  /** Cabecera lunes..domingo del mini-calendario: semana de referencia fija (2024-01-01 es lunes). */
  readonly weekdayLabels = Array.from({ length: 7 }, (_, i) => new Date(2024, 0, 1 + i));

  /** A diferencia de `tasks` (solo el espacio activo), consolida pendientes de TODOS los espacios. */
  readonly pendingTasks = computed(() =>
    [...this.pendingTaskEntries()].sort((a, b) => a.task.dueAt.localeCompare(b.task.dueAt)),
  );

  readonly pendingChecklistItems = computed(() =>
    [...this.checklist()]
      .filter((item) => !item.done)
      .sort((a, b) => a.date.localeCompare(b.date) || a.position - b.position),
  );

  constructor() {
    effect(() => {
      const workspaceId = this.activeWorkspaceStore.activeWorkspaceId();
      untracked(() => {
        this.loadWorkspaceAgenda(workspaceId);
        this.loadAllPendingTasks();
      });
    });

    effect(() => {
      const iso = this.selectedIso();
      untracked(() => {
        this.noteDraft.set(this.notesByDay().get(iso) ?? '');
      });
    });

    effect(() => {
      const iso = this.selectedIso();
      const connected = this.calendarStatus()?.connected ?? false;
      untracked(() => {
        if (connected) {
          this.loadCalendarEvents(iso);
        } else {
          this.calendarEvents.set([]);
        }
      });
    });

    effect(() => {
      const monthIndex = this.selectedMonthIndex();
      if (this.loading()) {
        return;
      }
      setTimeout(() => this.scrollMonthsRailTo(monthIndex));
    });

    const calendarParam = this.route.snapshot.queryParamMap.get('calendar');
    if (calendarParam === 'error') {
      this.calendarErrorMessage.set('No se pudo conectar con Google Calendar. Inténtalo de nuevo.');
    }
    this.loadCalendarStatus();
  }

  private loadWorkspaceAgenda(workspaceId: string | null): void {
    if (!workspaceId) {
      this.loading.set(false);
      this.errorMessage.set('No hay ningún espacio de trabajo activo todavía');
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.agendaService.getWorkspaceAgenda(workspaceId).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudo cargar la agenda');
      },
    });
    this.notesChecklistService.getNotes(workspaceId).subscribe({
      next: (notes) => {
        this.notes.set(notes);
        const current = notes.find((note) => note.date === this.selectedIso());
        this.noteDraft.set(current?.content ?? '');
      },
    });
    this.notesChecklistService.getChecklist(workspaceId).subscribe({
      next: (items) => this.checklist.set(items),
    });
  }

  /** Pendientes → Tareas: a diferencia del resto de la página, recorre todos los espacios de trabajo. */
  private loadAllPendingTasks(): void {
    this.workspaceService.listWorkspaces().subscribe({
      next: (workspaces) => {
        this.workspaces.set(workspaces);
        if (workspaces.length === 0) {
          this.pendingTaskEntries.set([]);
          return;
        }
        const requests = workspaces.map((workspace) =>
          this.agendaService.getWorkspaceAgenda(workspace.id).pipe(
            map((tasks) =>
              tasks
                .filter((task) => !task.completed)
                .map((task): PendingTaskEntry => ({ task, workspaceId: workspace.id, workspaceName: workspace.name })),
            ),
          ),
        );
        forkJoin(requests).subscribe({
          next: (results) => this.pendingTaskEntries.set(results.flat()),
          error: () => this.pendingTaskEntries.set([]),
        });
      },
      error: () => this.pendingTaskEntries.set([]),
    });
  }

  workspaceColor(name: string): string {
    return workspaceTabColor(name);
  }

  meetingStatusClass(event: CalendarEventView): 'is-ongoing' | 'is-past' | 'is-accepted' | 'is-declined' {
    const now = new Date();
    const start = new Date(event.start);
    const end = new Date(event.end);
    if (now >= start && now < end) {
      return 'is-ongoing';
    }
    if (now >= end) {
      return 'is-past';
    }
    return event.accepted ? 'is-accepted' : 'is-declined';
  }

  meetingStatusLabel(event: CalendarEventView): string {
    switch (this.meetingStatusClass(event)) {
      case 'is-ongoing':
        return 'En curso';
      case 'is-past':
        return 'Ya ha pasado';
      case 'is-declined':
        return 'No aceptada';
      default:
        return 'Aceptada';
    }
  }

  private loadCalendarStatus(): void {
    this.googleCalendarService.getStatus().subscribe({
      next: (status) => {
        this.calendarStatus.set(status);
        if (status.connected) {
          this.loadMeetingNotes();
        }
      },
      error: () => this.calendarStatus.set({ connected: false, email: null }),
    });
  }

  private loadMeetingNotes(): void {
    this.googleCalendarService.getMeetingNotes().subscribe({
      next: (notes) => {
        const drafts: Record<string, string> = {};
        for (const note of notes) {
          drafts[note.eventId] = note.content;
        }
        this.meetingNoteDrafts.set(drafts);
      },
    });
  }

  private loadCalendarEvents(date: string): void {
    this.calendarLoading.set(true);
    this.googleCalendarService.getEventsForDay(date).subscribe({
      next: (events) => {
        this.calendarEvents.set(events);
        this.calendarLoading.set(false);
      },
      error: () => {
        this.calendarEvents.set([]);
        this.calendarLoading.set(false);
      },
    });
  }

  connectGoogleCalendar(): void {
    this.googleCalendarService.getConnectUrl().subscribe({
      next: ({ url }) => {
        window.location.href = url;
      },
    });
  }

  disconnectGoogleCalendar(): void {
    this.googleCalendarService.disconnect().subscribe({
      next: () => {
        this.calendarStatus.set({ connected: false, email: null });
        this.calendarEvents.set([]);
        this.meetingNoteDrafts.set({});
      },
    });
  }

  meetingNoteFor(eventId: string): string {
    return this.meetingNoteDrafts()[eventId] ?? '';
  }

  hasMeetingNote(eventId: string): boolean {
    return this.meetingNoteFor(eventId).trim().length > 0;
  }

  openMeetingNotes(event: CalendarEventView): void {
    this.activeMeetingNote.set(event);
  }

  closeMeetingNotes(): void {
    const event = this.activeMeetingNote();
    if (event) {
      this.flushMeetingNote(event.id);
    }
    this.activeMeetingNote.set(null);
  }

  onMeetingNoteInput(eventId: string, value: string): void {
    this.meetingNoteDrafts.update((current) => ({ ...current, [eventId]: value }));
    if (this.meetingNoteSaveTimers[eventId]) {
      clearTimeout(this.meetingNoteSaveTimers[eventId]);
    }
    this.meetingNoteSaveTimers[eventId] = setTimeout(() => this.flushMeetingNote(eventId), NOTE_SAVE_DEBOUNCE_MS);
  }

  onMeetingNoteBlur(eventId: string): void {
    this.flushMeetingNote(eventId);
  }

  private flushMeetingNote(eventId: string): void {
    if (this.meetingNoteSaveTimers[eventId]) {
      clearTimeout(this.meetingNoteSaveTimers[eventId]);
      delete this.meetingNoteSaveTimers[eventId];
    }
    this.googleCalendarService.saveMeetingNote(eventId, this.meetingNoteFor(eventId)).subscribe();
  }

  generateMessageFor(eventId: string): string | null {
    return this.generateMessageByEvent()[eventId] ?? null;
  }

  generateFromMeeting(event: CalendarEventView): void {
    const workspaceId = this.activeWorkspaceStore.activeWorkspaceId();
    if (!workspaceId || this.generatingEventId()) {
      return;
    }
    this.flushMeetingNote(event.id);
    this.generatingEventId.set(event.id);
    this.googleCalendarService.generateFromMeeting(event.id, workspaceId, this.selectedIso(), event.title).subscribe({
      next: (result) => {
        this.generatingEventId.set(null);
        this.setGenerateMessage(event.id, this.describeGenerateResult(result));
        this.loadWorkspaceAgenda(workspaceId);
        this.loadAllPendingTasks();
      },
      error: () => {
        this.generatingEventId.set(null);
        this.setGenerateMessage(event.id, 'No se pudo generar. Inténtalo de nuevo.');
      },
    });
  }

  private setGenerateMessage(eventId: string, message: string): void {
    this.generateMessageByEvent.update((current) => ({ ...current, [eventId]: message }));
  }

  private describeGenerateResult(result: GenerateFromMeetingResult): string {
    const parts: string[] = [];
    if (result.tasksCreated > 0) {
      parts.push(`${result.tasksCreated} tarea${result.tasksCreated === 1 ? '' : 's'}`);
    }
    if (result.checklistItemsCreated > 0) {
      parts.push(`${result.checklistItemsCreated} apunte${result.checklistItemsCreated === 1 ? '' : 's'} de checklist`);
    }
    if (result.noteUpdated) {
      parts.push('nota del día actualizada');
    }
    return parts.length > 0 ? `Creado: ${parts.join(', ')}.` : 'No se encontró nada que crear en esta nota.';
  }

  prevDay(): void {
    this.flushNoteDraft();
    this.selectedDate.update((date) => addDays(date, -1));
  }

  nextDay(): void {
    this.flushNoteDraft();
    this.selectedDate.update((date) => addDays(date, 1));
  }

  goToday(): void {
    this.flushNoteDraft();
    this.selectedDate.set(startOfDay(new Date()));
  }

  selectDate(date: Date): void {
    this.flushNoteDraft();
    this.selectedDate.set(startOfDay(date));
  }

  isSelected(day: StripDay): boolean {
    return day.iso === this.selectedIso();
  }

  /** Centra el mes activo dentro del carril de meses, sin mover el scroll general de la página. */
  private scrollMonthsRailTo(monthIndex: number): void {
    const target = document.getElementById(`planner-month-${monthIndex}`);
    const container = target?.closest<HTMLElement>('.planner__months');
    if (!target || !container) {
      return;
    }
    const offset = target.offsetTop - container.clientHeight / 2 + target.clientHeight / 2;
    container.scrollTo({ top: offset, behavior: 'smooth' });
  }

  isSelectedDay(date: Date): boolean {
    return dateKey(date) === this.selectedIso();
  }

  isTodayDate(date: Date): boolean {
    return dateKey(date) === this.todayIso;
  }

  dayHasContent(date: Date): boolean {
    const iso = dateKey(date);
    return (
      this.tasksByDay().has(iso) ||
      this.checklistByDay().has(iso) ||
      (this.notesByDay().get(iso) ?? '').trim().length > 0
    );
  }

  dateFromKey(key: string): Date {
    return parseDateKey(key);
  }

  taskDueDate(task: AgendaTaskView): Date {
    return new Date(task.dueAt);
  }

  onNoteInput(value: string): void {
    this.noteDraft.set(value);
    if (this.noteSaveTimer) {
      clearTimeout(this.noteSaveTimer);
    }
    this.noteSaveTimer = setTimeout(() => this.flushNoteDraft(), NOTE_SAVE_DEBOUNCE_MS);
  }

  onNoteBlur(): void {
    this.flushNoteDraft();
  }

  private flushNoteDraft(): void {
    if (this.noteSaveTimer) {
      clearTimeout(this.noteSaveTimer);
      this.noteSaveTimer = null;
    }
    const workspaceId = this.activeWorkspaceStore.activeWorkspaceId();
    if (!workspaceId) {
      return;
    }
    const date = this.selectedIso();
    const content = this.noteDraft();
    if ((this.notesByDay().get(date) ?? '') === content) {
      return;
    }
    this.notesChecklistService.saveNote(workspaceId, date, content).subscribe({
      next: (saved) => {
        this.notes.update((current) => [...current.filter((note) => note.date !== saved.date), saved]);
      },
    });
  }

  addChecklistItem(): void {
    const text = this.newChecklistText().trim();
    const workspaceId = this.activeWorkspaceStore.activeWorkspaceId();
    if (!text || !workspaceId) {
      return;
    }
    this.notesChecklistService.createChecklistItem(workspaceId, this.selectedIso(), text).subscribe({
      next: (item) => {
        this.checklist.update((current) => [...current, item]);
        this.newChecklistText.set('');
      },
    });
  }

  toggleChecklistItem(item: ChecklistItemView): void {
    this.notesChecklistService.updateChecklistItem(item.id, { done: !item.done }).subscribe({
      next: (updated) => this.replaceChecklistItem(updated),
    });
  }

  deleteChecklistItem(item: ChecklistItemView): void {
    this.notesChecklistService.deleteChecklistItem(item.id).subscribe({
      next: () => this.checklist.update((current) => current.filter((i) => i.id !== item.id)),
    });
  }

  private replaceChecklistItem(updated: ChecklistItemView): void {
    this.checklist.update((current) => current.map((item) => (item.id === updated.id ? updated : item)));
  }
}
