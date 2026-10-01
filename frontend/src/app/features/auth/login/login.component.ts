import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

/** Espera (s) antes de cada reintento automático cuando el backend no responde; el último se repite. */
const RETRY_DELAYS_SECONDS = [5, 10, 20, 30];
const MAX_RETRIES = 10;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly submitting = signal(false);
  /** El backend (Render free) está arrancando en frío: se reintenta solo con espera creciente. */
  readonly waking = signal(false);
  readonly retryCountdown = signal(0);
  readonly attempt = signal(0);

  private retryTimer: ReturnType<typeof setInterval> | null = null;

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  onSubmit(): void {
    const emailControl = this.form.controls.email;
    emailControl.setValue(emailControl.value.trim());

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Revisa el email y la contraseña.');
      return;
    }

    this.errorMessage.set(null);
    this.attempt.set(0);
    this.attemptLogin();
  }

  cancelRetry(): void {
    this.stopTimer();
    this.waking.set(false);
    this.submitting.set(false);
    this.errorMessage.set('Inicio de sesión cancelado. Puedes volver a intentarlo.');
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  private attemptLogin(): void {
    this.submitting.set(true);
    const { email, password } = this.form.getRawValue();

    this.authService.login(email, password).subscribe({
      next: () => {
        this.waking.set(false);
        this.submitting.set(false);
        this.router.navigateByUrl('/agenda');
      },
      error: (error: unknown) => {
        if (this.isServerUnavailable(error) && this.attempt() < MAX_RETRIES) {
          this.scheduleRetry();
          return;
        }

        this.waking.set(false);
        this.submitting.set(false);
        this.errorMessage.set(
          this.isServerUnavailable(error)
            ? 'No se pudo conectar con el servidor. Inténtalo de nuevo en unos minutos.'
            : 'Email o contraseña incorrectos',
        );
      },
    });
  }

  /** Sin respuesta (0), timeout del proxy o 5xx: el backend no está listo, no es un fallo de credenciales. */
  private isServerUnavailable(error: unknown): boolean {
    return error instanceof HttpErrorResponse && (error.status === 0 || error.status >= 500);
  }

  private scheduleRetry(): void {
    const delays = RETRY_DELAYS_SECONDS;
    const delay = delays[Math.min(this.attempt(), delays.length - 1)];
    this.attempt.update((n) => n + 1);
    this.waking.set(true);
    this.retryCountdown.set(delay);

    this.stopTimer();
    this.retryTimer = setInterval(() => {
      const remaining = this.retryCountdown() - 1;
      this.retryCountdown.set(remaining);
      if (remaining <= 0) {
        this.stopTimer();
        this.attemptLogin();
      }
    }, 1000);
  }

  private stopTimer(): void {
    if (this.retryTimer !== null) {
      clearInterval(this.retryTimer);
      this.retryTimer = null;
    }
  }
}
