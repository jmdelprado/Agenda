import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ColumnView } from '../board.service';
import { BoardService } from '../board.service';

/**
 * Gestión de una columna existente (renombrar/eliminar, User Story 1, T045). Al intentar
 * eliminar la única columna del tablero, el backend responde 409 (DeleteColumnService) y
 * este componente muestra el mensaje de forma amigable en lugar de un error genérico.
 */
@Component({
  selector: 'app-column-manager',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './column-manager.component.html',
  styleUrl: './column-manager.component.css',
})
export class ColumnManagerComponent {
  private readonly boardService = inject(BoardService);

  @Input({ required: true }) column!: ColumnView;
  @Output() changed = new EventEmitter<void>();

  readonly renaming = signal(false);
  readonly draftName = signal('');
  readonly errorMessage = signal<string | null>(null);

  startRename(): void {
    this.draftName.set(this.column.name);
    this.errorMessage.set(null);
    this.renaming.set(true);
  }

  cancelRename(): void {
    this.renaming.set(false);
  }

  saveRename(): void {
    const name = this.draftName().trim();
    if (!name) {
      this.errorMessage.set('El nombre es obligatorio');
      return;
    }
    this.boardService.renameColumn(this.column.id, name).subscribe({
      next: () => {
        this.renaming.set(false);
        this.changed.emit();
      },
      error: () => this.errorMessage.set('No se pudo renombrar la columna'),
    });
  }

  delete(): void {
    if (!confirm(`¿Eliminar la columna "${this.column.name}" y todas sus tarjetas?`)) {
      return;
    }
    this.errorMessage.set(null);
    this.boardService.deleteColumn(this.column.id).subscribe({
      next: () => this.changed.emit(),
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(
          err.status === 409
            ? 'No se puede eliminar la única columna del tablero'
            : 'No se pudo eliminar la columna',
        );
      },
    });
  }
}
