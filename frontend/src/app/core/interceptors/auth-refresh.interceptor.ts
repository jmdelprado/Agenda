import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';

/**
 * El access token dura 15 minutos (app.jwt.access-token-expiration-minutes). Sin este
 * interceptor, en cuanto caduca, cualquier pantalla abierta empieza a recibir 401/403 en todas
 * sus peticiones y deja de poder cargar nada. Aquí se intenta renovar una vez con el refresh
 * token (30 días) y reintentar la petición original; si el refresh también falla, se cierra
 * sesión y se manda a /login en vez de dejar la app en un estado roto.
 */
export const authRefreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (req.url.includes('/auth/')) {
    return next(req);
  }

  return next(req).pipe(
    catchError((error: unknown) => {
      const isAuthError = error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403);
      if (!isAuthError || !authService.getRefreshToken()) {
        return throwError(() => error);
      }

      return authService.ensureFreshAccessToken().pipe(
        // Solo se cierra sesión si el backend rechaza el refresh token. Un fallo de red, timeout
        // o 5xx (p.ej. Render arrancando en frío) no invalida la sesión: se propaga el error
        // original y el usuario conserva sus tokens para reintentar.
        catchError((refreshError: unknown) => {
          if (
            refreshError instanceof HttpErrorResponse &&
            (refreshError.status === 400 || refreshError.status === 401 || refreshError.status === 403)
          ) {
            authService.logout();
            router.navigateByUrl('/login');
          }
          return throwError(() => error);
        }),
        switchMap((accessToken) =>
          next(req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })),
        ),
      );
    }),
  );
};
