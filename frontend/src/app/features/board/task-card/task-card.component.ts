import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { CdkDrag } from '@angular/cdk/drag-drop';
import { FormsModule } from '@angular/forms';

import { TaskView } from '../board.service';
import { TaskService } from '../task.service';

/**
 * Tarjeta de una tarea dentro del tablero Kanban (User Story 1, T044): permite editar
 * título/descripción in situ y eliminar con confirmación. El arrastre en sí lo gestiona
 * el `cdkDropList` del BoardComponent; esta tarjeta solo se marca como `cdkDrag`.
 */
@Component({
  selector: 'app-task-card',
  standalone: true,
  imports: [CommonModule, FormsModule, CdkDrag],
  templateUrl: './task-card.component.html',
  styleUrl: './task-card.component.css',
})
export class TaskCardComponent {
  private readonly taskService = inject(TaskService);

  @Input({ required: true }) task!: TaskView;
  @Output() changed = new EventEmitter<void>();

  readonly editing = signal(false);
  readonly draftTitle = signal('');
  readonly draftDescription = signal('');
  readonly errorMessage = signal<string | null>(null);

  readonly editingDueDate = signal(false);
  readonly draftDueAt = signal('');
  readonly draftReminderLeadMinutes = signal<number | null>(null);
  readonly dueDateErrorMessage = signal<string | null>(null);

  startEdit(): void {
    this.draftTitle.set(this.task.title);
    this.draftDescription.set(this.task.description ?? '');
    this.errorMessage.set(null);
    this.editing.set(true);
  }

  cancelEdit(): void {
    this.editing.set(false);
  }

  saveEdit(): void {
    const title = this.draftTitle().trim();
    if (!title) {
      this.errorMessage.set('El título es obligatorio');
      return;
    }
    this.taskService.updateTask(this.task.id, title, this.draftDescription().trim() || null).subscribe({
      next: () => {
        this.editing.set(false);
        this.changed.emit();
      },
      error: () => this.errorMessage.set('No se pudo guardar la tarea'),
    });
  }

  startDueDateEdit(): void {
    this.draftDueAt.set(this.task.dueAt ? this.toDatetimeLocal(this.task.dueAt) : '');
    this.draftReminderLeadMinutes.set(this.task.reminderLeadMinutes);
    this.dueDateErrorMessage.set(null);
    this.editingDueDate.set(true);
  }

  cancelDueDateEdit(): void {
    this.editingDueDate.set(false);
  }

  /** Guarda la fecha límite/antelación; un campo de fecha vacío la elimina (US2-AS4). */
  saveDueDate(): void {
    const raw = this.draftDueAt();
    const dueAt = raw ? new Date(raw).toISOString() : null;
    const reminderLeadMinutes = dueAt ? this.draftReminderLeadMinutes() : null;
    this.taskService.setDueDate(this.task.id, dueAt, reminderLeadMinutes).subscribe({
      next: () => {
        this.editingDueDate.set(false);
        this.changed.emit();
      },
      error: () => this.dueDateErrorMessage.set('No se pudo guardar la fecha límite'),
    });
  }

  clearDueDate(): void {
    this.taskService.setDueDate(this.task.id, null, null).subscribe({
      next: () => {
        this.editingDueDate.set(false);
        this.changed.emit();
      },
      error: () => this.dueDateErrorMessage.set('No se pudo eliminar la fecha límite'),
    });
  }

  private toDatetimeLocal(isoInstant: string): string {
    const date = new Date(isoInstant);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  delete(): void {
    if (!confirm(`¿Eliminar la tarjeta "${this.task.title}"?`)) {
      return;
    }
    this.taskService.deleteTask(this.task.id).subscribe({
      next: () => this.changed.emit(),
      error: () => this.errorMessage.set('No se pudo eliminar la tarea'),
    });
  }
}
