import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'agenda' },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'agenda',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/agenda/agenda.component').then((m) => m.AgendaComponent),
  },
  {
    path: 'board',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/board/board.component').then((m) => m.BoardComponent),
  },
  { path: '**', redirectTo: 'agenda' },
];
