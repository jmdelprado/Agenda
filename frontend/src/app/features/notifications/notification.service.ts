import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface NotificationView {
  id: string;
  message: string;
  reminderId: string;
  readAt: string | null;
  createdAt: string;
}

/**
 * Consume las notificaciones in-app (ver contracts/api-contracts.md § Reminders & Notifications).
 * No hay push en tiempo real (research.md §4): el panel hace *short polling* sobre {@link list}.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/notifications`;

  list(unreadOnly = false): Observable<NotificationView[]> {
    return this.http.get<NotificationView[]>(this.baseUrl, { params: { unreadOnly } });
  }

  markAsRead(notificationId: string): Observable<{ id: string; readAt: string }> {
    return this.http.patch<{ id: string; readAt: string }>(`${this.baseUrl}/${notificationId}/read`, {});
  }
}
