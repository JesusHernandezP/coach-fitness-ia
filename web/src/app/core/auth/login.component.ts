import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <p class="text-accent" style="font-size:0.65rem;font-weight:700;letter-spacing:0.1em;text-align:center">FITNESS AI COACH</p>
        <h1 style="text-align:center;margin:0.75rem 0 0.25rem">Bienvenido de vuelta</h1>
        <p class="text-accent" style="text-align:center;font-weight:600;margin-bottom:0.5rem">Tu coach de bolsillo</p>
        <p class="text-muted" style="text-align:center;font-size:0.8rem;margin-bottom:1.75rem">
          Inicia sesion para continuar tu progreso, chat y coaching sincronizado.
        </p>

        @if (error()) {
          <p style="color:var(--danger);margin-bottom:1rem;font-size:0.85rem">{{ error() }}</p>
        }

        <form (ngSubmit)="submit()">
          <div style="margin-bottom:1rem">
            <label class="label">Correo</label>
            <input class="input" type="email" [(ngModel)]="email" name="email" required />
          </div>
          <div style="margin-bottom:1.5rem">
            <label class="label">Contrasena</label>
            <input class="input" type="password" [(ngModel)]="password" name="password" required />
          </div>
          <button class="btn btn-primary" style="width:100%" type="submit" [disabled]="loading()">
            {{ loading() ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <a routerLink="/register" class="text-muted" style="display:block;text-align:center;margin-top:1rem;font-size:0.85rem">
          Crear cuenta
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
      background: var(--bg);
    }
    .auth-card { width: 380px; max-width: 95vw; }
  `],
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  loading = signal(false);
  error = signal('');

  submit() {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error.set('Credenciales incorrectas');
        this.loading.set(false);
      },
    });
  }
}
