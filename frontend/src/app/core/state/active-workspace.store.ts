import { Injectable, signal } from '@angular/core';

const ACTIVE_WORKSPACE_KEY = 'kanban_agenda_active_workspace_id';

function readStoredWorkspaceId(): string | null {
  try {
    return localStorage.getItem(ACTIVE_WORKSPACE_KEY);
  } catch {
    return null;
  }
}

/**
 * Estado reactivo del espacio de trabajo activo (User Story 3, T068). Persiste en localStorage
 * para sobrevivir recargas y expone un signal para que `BoardComponent`/`AgendaComponent` se
 * actualicen automáticamente al cambiar de pestaña, sin necesidad de recargar la página (SC-004).
 */
@Injectable({ providedIn: 'root' })
export class ActiveWorkspaceStore {
  readonly activeWorkspaceId = signal<string | null>(readStoredWorkspaceId());

  setActive(workspaceId: string): void {
    try {
      localStorage.setItem(ACTIVE_WORKSPACE_KEY, workspaceId);
    } catch {
      // almacenamiento no disponible; el workspace activo no persistirá entre recargas
    }
    this.activeWorkspaceId.set(workspaceId);
  }

  clear(): void {
    try {
      localStorage.removeItem(ACTIVE_WORKSPACE_KEY);
    } catch {
      // almacenamiento no disponible
    }
    this.activeWorkspaceId.set(null);
  }

  get(): string | null {
    return this.activeWorkspaceId();
  }
}
