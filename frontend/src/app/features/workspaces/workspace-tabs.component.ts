import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { WorkspaceService, WorkspaceSummary } from './workspace.service';
import { ActiveWorkspaceStore } from '../../core/state/active-workspace.store';

/**
 * Gestión de espacios de trabajo (pestañas): listar, crear, renombrar, eliminar (con
 * confirmación, FR-012) y cambiar el espacio activo (User Story 3, T067-T068). Se muestra en
 * `AppComponent` para estar visible tanto en el tablero como en la agenda.
 */
@Component({
  selector: 'app-workspace-tabs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './workspace-tabs.component.html',
  styleUrl: './workspace-tabs.component.css',
})
export class WorkspaceTabsComponent implements OnInit {
  private readonly workspaceService = inject(WorkspaceService);
  private readonly activeWorkspaceStore = inject(ActiveWorkspaceStore);

  readonly workspaces = signal<WorkspaceSummary[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly activeWorkspaceId = this.activeWorkspaceStore.activeWorkspaceId;
  readonly creating = signal(false);
  readonly newWorkspaceName = signal('');
  readonly renamingId = signal<string | null>(null);
  readonly renameValue = signal('');

  ngOnInit(): void {
    this.refresh();
  }

  private refresh(selectIfNoneActive = true): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.workspaceService.listWorkspaces().subscribe({
      next: (workspaces) => {
        if (workspaces.length === 0) {
          this.createDefaultWorkspace();
          return;
        }
        this.workspaces.set(workspaces);
        this.loading.set(false);
        this.ensureActiveWorkspaceIsValid(workspaces, selectIfNoneActive);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudieron cargar los espacios de trabajo');
      },
    });
  }

  /** Primer uso (sin espacios de trabajo todavía): crea uno para que Agenda/Tablero no arranquen vacíos. */
  private createDefaultWorkspace(): void {
    this.workspaceService.createWorkspace('Mi primer espacio de trabajo').subscribe({
      next: () => this.refresh(false),
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudo crear el espacio de trabajo inicial');
      },
    });
  }

  private ensureActiveWorkspaceIsValid(workspaces: WorkspaceSummary[], selectIfNoneActive: boolean): void {
    const activeId = this.activeWorkspaceStore.get();
    const stillExists = activeId !== null && workspaces.some((w) => w.id === activeId);
    if (!stillExists && selectIfNoneActive && workspaces.length > 0) {
      this.activeWorkspaceStore.setActive(workspaces[0].id);
    }
  }

  isActive(workspace: WorkspaceSummary): boolean {
    return this.activeWorkspaceId() === workspace.id;
  }

  select(workspace: WorkspaceSummary): void {
    this.activeWorkspaceStore.setActive(workspace.id);
  }

  startCreating(): void {
    this.creating.set(true);
    this.newWorkspaceName.set('');
  }

  cancelCreating(): void {
    this.creating.set(false);
    this.newWorkspaceName.set('');
  }

  confirmCreate(): void {
    const name = this.newWorkspaceName().trim();
    if (!name) {
      return;
    }
    this.workspaceService.createWorkspace(name).subscribe({
      next: () => {
        this.creating.set(false);
        this.newWorkspaceName.set('');
        // createWorkspace ya marca el nuevo workspace como activo (ActiveWorkspaceStore).
        this.refresh(false);
      },
      error: () => this.errorMessage.set('No se pudo crear el espacio de trabajo'),
    });
  }

  startRenaming(workspace: WorkspaceSummary): void {
    this.renamingId.set(workspace.id);
    this.renameValue.set(workspace.name);
  }

  cancelRenaming(): void {
    this.renamingId.set(null);
    this.renameValue.set('');
  }

  confirmRename(workspace: WorkspaceSummary): void {
    const name = this.renameValue().trim();
    if (!name) {
      return;
    }
    this.workspaceService.renameWorkspace(workspace.id, name).subscribe({
      next: () => {
        this.renamingId.set(null);
        this.refresh(false);
      },
      error: () => this.errorMessage.set('No se pudo renombrar el espacio de trabajo'),
    });
  }

  remove(workspace: WorkspaceSummary): void {
    const confirmed = confirm(
        `¿Eliminar el espacio de trabajo "${workspace.name}"? Se cancelarán sus recordatorios pendientes.`);
    if (!confirmed) {
      return;
    }
    this.workspaceService.deleteWorkspace(workspace.id).subscribe({
      next: () => {
        if (this.activeWorkspaceId() === workspace.id) {
          this.activeWorkspaceStore.clear();
        }
        this.refresh(true);
      },
      error: () => this.errorMessage.set('No se pudo eliminar el espacio de trabajo'),
    });
  }
}
