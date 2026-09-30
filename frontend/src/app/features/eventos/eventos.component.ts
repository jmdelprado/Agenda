import { Component } from '@angular/core';

/** Espacio de la agenda personal donde se incorporarán los eventos importados de X (specs/002-importar-eventos-x). */
@Component({
  selector: 'app-eventos',
  standalone: true,
  template: `
    <main class="eventos">
      <h1>Eventos</h1>
      <p>Aquí aparecerán los eventos importados desde tus cuentas de X.</p>
    </main>
  `,
  styles: [
    `
      .eventos {
        max-width: 720px;
        margin: 2rem auto;
        padding: 0 1rem;
      }
    `,
  ],
})
export class EventosComponent {}
