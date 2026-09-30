import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { interval, startWith, switchMap } from 'rxjs';

import { NotificationService, NotificationView } from './notification.service';

const POLL_INTERVAL_MS = 30_000;

/**
 * Panel de notificaciones in-app (User Story 2, T061): se refresca mediante *short polling*
 * cada 30s (research.md §4) en vez de push en tiempo real.
 */
@Component({
  selector: 'app-notifications-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications-panel.component.html',
  styleUrl: './notifications-panel.component.css',
})
export class NotificationsPanelComponent {
  private readonly notificationService = inject(NotificationService);

  readonly open = signal(false);
  readonly notifications = signal<NotificationView[]>([]);

  readonly unreadCount = () => this.notifications().filter((n) => !n.readAt).length;

  constructor() {
    interval(POLL_INTERVAL_MS)
      .pipe(startWith(0), switchMap(() => this.notificationService.list()), takeUntilDestroyed())
      .subscribe({
        next: (notifications) => this.notifications.set(notifications),
        error: () => {
          // El polling continúa en el siguiente tick; un fallo puntual no bloquea el panel.
        },
      });
  }

  toggle(): void {
    this.open.update((value) => !value);
  }

  markAsRead(notification: NotificationView): void {
    if (notification.readAt) {
      return;
    }
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () =>
        this.notifications.update((list) =>
          list.map((n) => (n.id === notification.id ? { ...n, readAt: new Date().toISOString() } : n)),
        ),
    });
  }
}
