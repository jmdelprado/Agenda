import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface DayNoteView {
  date: string;
  content: string;
}

export interface ChecklistItemView {
  id: string;
  date: string;
  text: string;
  done: boolean;
  position: number;
}

/**
 * Notas de texto libre y checklist rápida de la Agenda de papel — independientes de las
 * tarjetas Kanban. Cada recurso se carga entero para el espacio de trabajo en un único GET
 * (igual que `AgendaService.getWorkspaceAgenda`) y el componente lo agrupa por día.
 */
@Injectable({ providedIn: 'root' })
export class NotesChecklistService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getNotes(workspaceId: string): Observable<DayNoteView[]> {
    return this.http.get<DayNoteView[]>(`${this.baseUrl}/workspaces/${workspaceId}/notes`);
  }

  saveNote(workspaceId: string, date: string, content: string): Observable<DayNoteView> {
    return this.http.patch<DayNoteView>(`${this.baseUrl}/workspaces/${workspaceId}/notes/${date}`, { content });
  }

  getChecklist(workspaceId: string): Observable<ChecklistItemView[]> {
    return this.http.get<ChecklistItemView[]>(`${this.baseUrl}/workspaces/${workspaceId}/checklist`);
  }

  createChecklistItem(workspaceId: string, date: string, text: string): Observable<ChecklistItemView> {
    return this.http.post<ChecklistItemView>(`${this.baseUrl}/workspaces/${workspaceId}/checklist`, { date, text });
  }

  updateChecklistItem(itemId: string, changes: { done?: boolean; text?: string }): Observable<ChecklistItemView> {
    return this.http.patch<ChecklistItemView>(`${this.baseUrl}/checklist/${itemId}`, changes);
  }

  deleteChecklistItem(itemId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/checklist/${itemId}`);
  }
}
