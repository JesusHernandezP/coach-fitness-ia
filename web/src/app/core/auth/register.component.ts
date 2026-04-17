import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <p class="text-accent" style="font-size:0.65rem;font-weight:700;letter-spacing:0.1em;text-align:center">FITNESS AI COACH</p>
        <h1 style="text-align:center;margin:0.75rem 0 1.5rem">Crear cuenta</h1>

        @if (error()) {
          <p style="color:var(--danger);margin-bottom:1rem;font-size:0.85rem">{{ error() }}</p>
        }

        <form (ngSubmit)="submit()">
          <div style="margin-bottom:1rem">
            <label class="label">Correo</label>
            <input class="input" type="email" [(ngModel)]="email" name="email" required />
          </div>
          <div style="margin-bottom:1.5rem">
            <label class="label">Contrasena (min. 8 caracteres)</label>
            <input class="input" type="password" [(ngModel)]="password" name="password" required minlength="8" />
          </div>
          <button class="btn btn-primary" style="width:100%" type="submit" [disabled]="loading()">
            {{ loading() ? 'Creando cuenta...' : 'Crear cuenta' }}
          </button>
        </form>

        <a routerLink="/login" class="text-muted" style="display:block;text-align:center;margin-top:1rem;font-size:0.85rem">
          Ya tengo cuenta
        </a>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .auth-card { width: 380px; max-width: 95vw; }
  `],
})
export class RegisterComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  loading = signal(false);
  error = signal('');

  submit() {
    this.loading.set(true);
    this.error.set('');
    this.auth.register(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Error al registrar');
        this.loading.set(false);
      },
    });
  }
}
