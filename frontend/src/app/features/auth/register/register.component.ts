import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.submitting.set(true);
    const { email, password } = this.form.getRawValue();

    this.authService.register(email, password).subscribe({
      next: () => {
        this.authService.login(email, password).subscribe({
          next: () => {
            this.submitting.set(false);
            this.router.navigateByUrl('/agenda');
          },
          error: () => {
            this.submitting.set(false);
            this.router.navigateByUrl('/login');
          },
        });
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('No se pudo completar el registro. ¿Ya existe una cuenta con ese email?');
      },
    });
  }
}
