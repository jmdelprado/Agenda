import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface AgendaTaskView {
  taskId: string;
  title: string;
  dueAt: string;
  overdue: boolean;
  completed: boolean;
}

/**
 * Consume la agenda de un espacio de trabajo (FR-004) — ver contracts/api-contracts.md § Agenda.
 */
@Injectable({ providedIn: 'root' })
export class AgendaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getWorkspaceAgenda(workspaceId: string): Observable<AgendaTaskView[]> {
    return this.http.get<AgendaTaskView[]>(`${this.baseUrl}/workspaces/${workspaceId}/agenda`);
  }
}
