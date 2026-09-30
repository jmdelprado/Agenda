import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface TaskView {
  id: string;
  title: string;
  description: string | null;
  completed: boolean;
  dueAt: string | null;
  reminderLeadMinutes: number | null;
  overdue: boolean;
}

export interface ColumnView {
  id: string;
  name: string;
  position: number;
  tasks: TaskView[];
}

export interface BoardView {
  boardId: string;
  columns: ColumnView[];
}

export interface ColumnResult {
  id: string;
  name: string;
  position: number;
}

/** Consume el tablero y sus columnas (ver contracts/api-contracts.md § Boards & Columns). */
@Injectable({ providedIn: 'root' })
export class BoardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getBoard(workspaceId: string): Observable<BoardView> {
    return this.http.get<BoardView>(`${this.baseUrl}/workspaces/${workspaceId}/board`);
  }

  createColumn(workspaceId: string, name: string): Observable<ColumnResult> {
    return this.http.post<ColumnResult>(`${this.baseUrl}/workspaces/${workspaceId}/board/columns`, { name });
  }

  renameColumn(columnId: string, name: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/boards/columns/${columnId}`, { name });
  }

  deleteColumn(columnId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/boards/columns/${columnId}`);
  }
}
