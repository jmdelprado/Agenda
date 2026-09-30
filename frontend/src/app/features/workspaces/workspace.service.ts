import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ActiveWorkspaceStore } from '../../core/state/active-workspace.store';

export interface ColumnSummary {
  id: string;
  name: string;
  position: number;
}

export interface BoardSummary {
  id: string;
  columns: ColumnSummary[];
}

export interface WorkspaceResponse {
  id: string;
  name: string;
  createdAt: string;
  board: BoardSummary;
}

export interface WorkspaceSummary {
  id: string;
  name: string;
  createdAt: string;
}

/**
 * Gestiona los espacios de trabajo (pestañas, ver contracts/api-contracts.md § Workspaces) y el
 * espacio activo, delegando el estado reactivo en {@link ActiveWorkspaceStore} (User Story 3, T068).
 * `getActiveWorkspaceId`/`setActiveWorkspaceId` se conservan como atajos síncronos usados por
 * `BoardComponent`/`AgendaComponent`.
 */
@Injectable({ providedIn: 'root' })
export class WorkspaceService {
  private readonly http = inject(HttpClient);
  private readonly activeWorkspaceStore = inject(ActiveWorkspaceStore);
  private readonly baseUrl = `${environment.apiBaseUrl}/workspaces`;

  createWorkspace(name: string): Observable<WorkspaceResponse> {
    return this.http.post<WorkspaceResponse>(this.baseUrl, { name }).pipe(
      tap((workspace) => this.setActiveWorkspaceId(workspace.id)),
    );
  }

  listWorkspaces(): Observable<WorkspaceSummary[]> {
    return this.http.get<WorkspaceSummary[]>(this.baseUrl);
  }

  renameWorkspace(workspaceId: string, name: string): Observable<{ id: string; name: string }> {
    return this.http.patch<{ id: string; name: string }>(`${this.baseUrl}/${workspaceId}`, { name });
  }

  deleteWorkspace(workspaceId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${workspaceId}`);
  }

  getActiveWorkspaceId(): string | null {
    return this.activeWorkspaceStore.get();
  }

  setActiveWorkspaceId(workspaceId: string): void {
    this.activeWorkspaceStore.setActive(workspaceId);
  }
}
