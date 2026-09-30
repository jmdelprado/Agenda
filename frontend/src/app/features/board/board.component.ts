import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { FormsModule } from '@angular/forms';

import { BoardService, BoardView, TaskView } from './board.service';
import { ColumnManagerComponent } from './column-manager/column-manager.component';
import { TaskCardComponent } from './task-card/task-card.component';
import { TaskService } from './task.service';
import { WorkspaceService } from '../workspaces/workspace.service';
import { ActiveWorkspaceStore } from '../../core/state/active-workspace.store';

/**
 * Tablero Kanban (User Story 1, T043-T046): columnas con arrastrar-y-soltar de tarjetas vía
 * Angular CDK, creación/edición/eliminación de tarjetas y gestión de columnas. Sustituye al
 * placeholder creado en Phase 2 (Foundational). Desde User Story 3 (T068) reacciona al cambio
 * de espacio de trabajo activo (pestañas) sin necesidad de recargar la página (SC-004).
 */
@Component({
  selector: 'app-board',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, TaskCardComponent, ColumnManagerComponent],
  templateUrl: './board.component.html',
  styleUrl: './board.component.css',
})
export class BoardComponent {
  private readonly workspaceService = inject(WorkspaceService);
  private readonly boardService = inject(BoardService);
  private readonly taskService = inject(TaskService);
  private readonly activeWorkspaceStore = inject(ActiveWorkspaceStore);

  readonly board = signal<BoardView | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly newColumnName = signal('');
  readonly newTaskTitleByColumn = signal<Record<string, string>>({});

  readonly dropListIds = computed(() => (this.board()?.columns ?? []).map((c) => this.dropListId(c.id)));

  constructor() {
    effect(() => {
      const workspaceId = this.activeWorkspaceStore.activeWorkspaceId();
      if (workspaceId) {
        untracked(() => this.loadBoard(workspaceId));
      }
    });
  }

  private loadBoard(workspaceId: string): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.boardService.getBoard(workspaceId).subscribe({
      next: (board) => {
        this.board.set(board);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudo cargar el tablero');
      },
    });
  }

  reload(): void {
    const workspaceId = this.workspaceService.getActiveWorkspaceId();
    if (workspaceId) {
      this.loadBoard(workspaceId);
    }
  }

  dropListId(columnId: string): string {
    return `column-${columnId}`;
  }

  onDrop(event: CdkDragDrop<TaskView[]>, targetColumnId: string): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }
    const task = event.previousContainer.data[event.previousIndex];
    transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    this.taskService.moveTask(task.id, targetColumnId).subscribe({
      error: () => {
        this.errorMessage.set('No se pudo mover la tarjeta');
        this.reload();
      },
    });
  }

  addColumn(): void {
    const workspaceId = this.workspaceService.getActiveWorkspaceId();
    const name = this.newColumnName().trim();
    if (!workspaceId || !name) {
      return;
    }
    this.boardService.createColumn(workspaceId, name).subscribe({
      next: () => {
        this.newColumnName.set('');
        this.reload();
      },
      error: () => this.errorMessage.set('No se pudo crear la columna'),
    });
  }

  newTaskTitleFor(columnId: string): string {
    return this.newTaskTitleByColumn()[columnId] ?? '';
  }

  setNewTaskTitle(columnId: string, value: string): void {
    this.newTaskTitleByColumn.update((current) => ({ ...current, [columnId]: value }));
  }

  addTask(columnId: string): void {
    const title = this.newTaskTitleFor(columnId).trim();
    if (!title) {
      return;
    }
    this.taskService.createTask(columnId, title, null).subscribe({
      next: () => {
        this.setNewTaskTitle(columnId, '');
        this.reload();
      },
      error: () => this.errorMessage.set('No se pudo crear la tarjeta'),
    });
  }
}
