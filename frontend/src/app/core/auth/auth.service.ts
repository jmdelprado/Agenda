import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, finalize, map, shareReplay, tap, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';

const ACCESS_TOKEN_KEY = 'kanban_agenda_access_token';
const REFRESH_TOKEN_KEY = 'kanban_agenda_refresh_token';

export interface RegisterResponse {
  userId: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export interface RefreshResponse {
  accessToken: string;
  refreshToken: string;
}

/**
 * Gestiona el registro/login/refresh contra la API (ver contracts/api-contracts.md § Auth)
 * y el almacenamiento del par de tokens JWT (ver research.md §2).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly accessTokenSignal = signal<string | null>(this.readToken(ACCESS_TOKEN_KEY));
  readonly isAuthenticated = computed(() => this.accessTokenSignal() !== null);

  /** Refresco en curso compartido: evita que varias peticiones en paralelo (T068: tablero,
   * agenda, notificaciones...) disparen cada una su propio refresh cuando el access token
   * caduca (15 min) — el refresh token rota en cada llamada, así que la segunda invalidaría a
   * la primera si no se comparte la misma petición en curso. */
  private refreshInFlight$: Observable<RefreshResponse> | null = null;

  register(email: string, password: string): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, { email, password });
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, { email, password }).pipe(
      tap((response) => this.storeTokens(response.accessToken, response.refreshToken)),
    );
  }

  /** El refresh token rota en cada llamada (T073): el backend devuelve uno nuevo que sustituye al usado. */
  refresh(): Observable<RefreshResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<RefreshResponse>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      tap((response) => this.storeTokens(response.accessToken, response.refreshToken)),
    );
  }

  /** Igual que `refresh()`, pero comparte la petición en curso entre llamadas concurrentes. */
  ensureFreshAccessToken(): Observable<string> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No hay refresh token disponible'));
    }

    if (!this.refreshInFlight$) {
      this.refreshInFlight$ = this.refresh().pipe(
        shareReplay(1),
        finalize(() => {
          this.refreshInFlight$ = null;
        }),
      );
    }

    return this.refreshInFlight$.pipe(map((response) => response.accessToken));
  }

  logout(): void {
    this.removeToken(ACCESS_TOKEN_KEY);
    this.removeToken(REFRESH_TOKEN_KEY);
    this.accessTokenSignal.set(null);
  }

  getAccessToken(): string | null {
    return this.accessTokenSignal();
  }

  getRefreshToken(): string | null {
    return this.readToken(REFRESH_TOKEN_KEY);
  }

  private storeTokens(accessToken: string, refreshToken: string): void {
    this.storeAccessToken(accessToken);
    this.writeToken(REFRESH_TOKEN_KEY, refreshToken);
  }

  private storeAccessToken(accessToken: string): void {
    this.writeToken(ACCESS_TOKEN_KEY, accessToken);
    this.accessTokenSignal.set(accessToken);
  }

  private readToken(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private writeToken(key: string, value: string): void {
    try {
      localStorage.setItem(key, value);
    } catch {
      // almacenamiento no disponible (p.ej. modo privado); la sesión no persistirá entre recargas
    }
  }

  private removeToken(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch {
      // no-op
    }
  }
}
