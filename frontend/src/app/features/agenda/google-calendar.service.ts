import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface CalendarStatus {
  connected: boolean;
  email: string | null;
}

export interface CalendarEventView {
  id: string;
  title: string;
  start: string;
  end: string;
  allDay: boolean;
  location: string | null;
  htmlLink: string | null;
  accepted: boolean;
}

export interface MeetingNoteView {
  eventId: string;
  content: string;
}

export interface GenerateFromMeetingResult {
  tasksCreated: number;
  checklistItemsCreated: number;
  noteUpdated: boolean;
}

/** Conexión OAuth con Google Calendar y consulta de los eventos de un día, para el panel "Reuniones" de la Agenda. */
@Injectable({ providedIn: 'root' })
export class GoogleCalendarService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/calendar/google`;

  getStatus(): Observable<CalendarStatus> {
    return this.http.get<CalendarStatus>(`${this.baseUrl}/status`);
  }

  getConnectUrl(): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(`${this.baseUrl}/connect-url`);
  }

  disconnect(): Observable<void> {
    return this.http.delete<void>(this.baseUrl);
  }

  getEventsForDay(date: string): Observable<CalendarEventView[]> {
    return this.http.get<CalendarEventView[]>(`${this.baseUrl}/events`, { params: { date } });
  }

  getMeetingNotes(): Observable<MeetingNoteView[]> {
    return this.http.get<MeetingNoteView[]>(`${this.baseUrl}/notes`);
  }

  saveMeetingNote(eventId: string, content: string): Observable<MeetingNoteView> {
    return this.http.patch<MeetingNoteView>(`${this.baseUrl}/notes/${eventId}`, { content });
  }

  generateFromMeeting(
    eventId: string,
    workspaceId: string,
    date: string,
    meetingTitle: string,
  ): Observable<GenerateFromMeetingResult> {
    return this.http.post<GenerateFromMeetingResult>(`${this.baseUrl}/notes/${eventId}/generate`, {
      workspaceId,
      date,
      meetingTitle,
    });
  }
}
