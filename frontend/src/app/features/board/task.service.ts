import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TaskView } from './board.service';

/** Consume las tarjetas/tareas (ver contracts/api-contracts.md § Tasks). */
@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  createTask(columnId: string, title: string, description: string | null): Observable<TaskView> {
    return this.http.post<TaskView>(`${this.baseUrl}/boards/columns/${columnId}/tasks`, {
      title,
      description,
    });
  }

  updateTask(taskId: string, title: string, description: string | null): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/tasks/${taskId}`, { title, description });
  }

  moveTask(taskId: string, targetColumnId: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/tasks/${taskId}/move`, { columnId: targetColumnId });
  }

  /** {@code dueAt = null} elimina la fecha límite (y cancela el recordatorio pendiente, US2-AS4). */
  setDueDate(taskId: string, dueAt: string | null, reminderLeadMinutes: number | null): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/tasks/${taskId}/due-date`, { dueAt, reminderLeadMinutes });
  }

  deleteTask(taskId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tasks/${taskId}`);
  }
}
