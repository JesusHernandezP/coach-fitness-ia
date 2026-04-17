import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="auth-layout">
      <!-- Left panel: branding -->
      <aside class="brand-panel">
        <div class="brand-orb orb-1"></div>
        <div class="brand-orb orb-2"></div>
        <div class="brand-orb orb-3"></div>

        <div class="brand-content">
          <span class="brand-eyebrow">Tu coach de bolsillo</span>
          <h1 class="brand-title">FITNESS<br>AI<br>COACH</h1>
          <p class="brand-sub">Nutricion · Entrenamiento · IA</p>

          <ul class="brand-features">
            <li><span class="feat-dot"></span>Macros calculados con precision</li>
            <li><span class="feat-dot"></span>Historial de peso &amp; actividad</li>
            <li><span class="feat-dot"></span>Chat con nutricionista IA 24/7</li>
          </ul>
        </div>

        <div class="brand-stats">
          <div class="stat"><span class="stat-val">2360</span><span class="stat-lbl">kcal objetivo</span></div>
          <div class="stat-divider"></div>
          <div class="stat"><span class="stat-val">160g</span><span class="stat-lbl">proteina</span></div>
          <div class="stat-divider"></div>
          <div class="stat"><span class="stat-val">AI</span><span class="stat-lbl">coach</span></div>
        </div>
      </aside>

      <!-- Right panel: form -->
      <main class="form-panel">
        <div class="form-card">
          <div class="form-header">
            <p class="form-eyebrow">Bienvenido de vuelta</p>
            <h2 class="form-title">Inicia sesion</h2>
            <p class="form-hint">Continua tu progreso donde lo dejaste</p>
          </div>

          @if (error()) {
            <div class="alert-error">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5"/><path d="M8 5v3.5M8 11h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
              {{ error() }}
            </div>
          }

          <form (ngSubmit)="submit()" class="form-body">
            <div class="field">
              <label class="label" for="email">Correo electronico</label>
              <div class="input-wrap">
                <svg class="input-icon" width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="1.5" y="3.5" width="13" height="9" rx="1.5" stroke="currentColor" stroke-width="1.3"/><path d="M1.5 5.5l6.5 4 6.5-4" stroke="currentColor" stroke-width="1.3"/></svg>
                <input class="input has-icon" id="email" type="email" [(ngModel)]="email" name="email" placeholder="tu@correo.com" required autocomplete="email" />
              </div>
            </div>

            <div class="field">
              <label class="label" for="password">Contrasena</label>
              <div class="input-wrap">
                <svg class="input-icon" width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" stroke-width="1.3"/><path d="M5 7V5a3 3 0 0 1 6 0v2" stroke="currentColor" stroke-width="1.3"/></svg>
                <input class="input has-icon" id="password" type="password" [(ngModel)]="password" name="password" placeholder="••••••••" required autocomplete="current-password" />
              </div>
            </div>

            <button class="btn btn-primary btn-submit" type="submit" [disabled]="loading()">
              @if (loading()) {
                <span class="spinner"></span> Entrando...
              } @else {
                Entrar
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 7h8M7 3l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              }
            </button>
          </form>

          <p class="form-switch">
            ¿No tienes cuenta?
            <a routerLink="/register" class="form-link">Crear cuenta</a>
          </p>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .auth-layout {
      min-height: 100vh;
      display: grid;
      grid-template-columns: 1fr 1fr;
    }

    /* ── Brand panel ── */
    .brand-panel {
      position: relative;
      background: #0a0a0a;
      padding: 3rem;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      overflow: hidden;
      border-right: 1px solid #1e1e1e;
    }

    .brand-orb {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      pointer-events: none;
      animation: float 8s ease-in-out infinite;
    }
    .orb-1 { width: 320px; height: 320px; background: radial-gradient(circle, rgba(212,178,0,0.18) 0%, transparent 70%); top: -60px; right: -60px; animation-delay: 0s; }
    .orb-2 { width: 200px; height: 200px; background: radial-gradient(circle, rgba(212,178,0,0.10) 0%, transparent 70%); bottom: 100px; left: -40px; animation-delay: -3s; }
    .orb-3 { width: 140px; height: 140px; background: radial-gradient(circle, rgba(212,178,0,0.08) 0%, transparent 70%); top: 50%; left: 40%; animation-delay: -5s; }

    @keyframes float {
      0%, 100% { transform: translateY(0) scale(1); }
      50% { transform: translateY(-20px) scale(1.05); }
    }

    .brand-content { position: relative; z-index: 1; animation: fadeSlideUp 0.7s ease both; }

    .brand-eyebrow {
      font-family: 'DM Sans', sans-serif;
      font-size: 0.7rem;
      font-weight: 500;
      letter-spacing: 0.18em;
      text-transform: uppercase;
      color: var(--accent);
      display: block;
      margin-bottom: 1rem;
    }

    .brand-title {
      font-family: 'Bebas Neue', sans-serif;
      font-size: clamp(3.5rem, 6vw, 5.5rem);
      line-height: 0.92;
      letter-spacing: 0.02em;
      color: #f0f0f0;
      margin-bottom: 1rem;
    }

    .brand-sub {
      font-size: 0.8rem;
      color: #555;
      letter-spacing: 0.15em;
      text-transform: uppercase;
      margin-bottom: 2.5rem;
    }

    .brand-features {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .brand-features li {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-size: 0.875rem;
      color: #999;
    }

    .feat-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--accent);
      flex-shrink: 0;
      box-shadow: 0 0 8px rgba(212,178,0,0.6);
    }

    .brand-stats {
      position: relative;
      z-index: 1;
      display: flex;
      align-items: center;
      gap: 1.5rem;
      animation: fadeSlideUp 0.7s 0.15s ease both;
    }

    .stat { display: flex; flex-direction: column; gap: 0.25rem; }
    .stat-val { font-family: 'Bebas Neue', sans-serif; font-size: 1.8rem; color: var(--accent); line-height: 1; }
    .stat-lbl { font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.1em; color: #555; }
    .stat-divider { width: 1px; height: 40px; background: #2a2a2a; }

    /* ── Form panel ── */
    .form-panel {
      background: var(--bg);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
    }

    .form-card {
      width: 100%;
      max-width: 400px;
      animation: fadeSlideUp 0.6s 0.1s ease both;
    }

    .form-header { margin-bottom: 2rem; }
    .form-eyebrow {
      font-size: 0.7rem;
      font-weight: 500;
      letter-spacing: 0.15em;
      text-transform: uppercase;
      color: var(--accent);
      margin-bottom: 0.5rem;
    }
    .form-title {
      font-family: 'Bebas Neue', sans-serif;
      font-size: 2.5rem;
      letter-spacing: 0.03em;
      color: #f0f0f0;
      margin-bottom: 0.4rem;
    }
    .form-hint { font-size: 0.83rem; color: #666; }

    .alert-error {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: rgba(224, 82, 82, 0.1);
      border: 1px solid rgba(224, 82, 82, 0.25);
      border-radius: 8px;
      color: #e05252;
      font-size: 0.83rem;
      padding: 0.7rem 1rem;
      margin-bottom: 1.25rem;
    }

    .form-body { display: flex; flex-direction: column; gap: 1.25rem; }

    .field { display: flex; flex-direction: column; }

    .input-wrap { position: relative; }
    .input-icon {
      position: absolute;
      left: 0.875rem;
      top: 50%;
      transform: translateY(-50%);
      color: #555;
      pointer-events: none;
      transition: color 0.15s;
    }
    .input-wrap:focus-within .input-icon { color: var(--accent); }

    .input.has-icon { padding-left: 2.5rem; }

    .btn-submit {
      width: 100%;
      padding: 0.75rem;
      font-size: 0.9rem;
      border-radius: 10px;
      margin-top: 0.5rem;
      transition: background 0.2s, transform 0.1s, box-shadow 0.2s;
    }
    .btn-submit:hover:not(:disabled) {
      box-shadow: 0 0 20px rgba(212,178,0,0.25);
      transform: translateY(-1px);
    }
    .btn-submit:active:not(:disabled) { transform: translateY(0); }

    .spinner {
      width: 14px;
      height: 14px;
      border: 2px solid rgba(0,0,0,0.25);
      border-top-color: #000;
      border-radius: 50%;
      display: inline-block;
      animation: spin 0.7s linear infinite;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    .form-switch {
      text-align: center;
      font-size: 0.83rem;
      color: #666;
      margin-top: 1.5rem;
    }
    .form-link {
      color: var(--accent);
      text-decoration: none;
      font-weight: 600;
      margin-left: 0.25rem;
      transition: color 0.15s;
    }
    .form-link:hover { color: var(--accent-hover); }

    @keyframes fadeSlideUp {
      from { opacity: 0; transform: translateY(16px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    /* ── Responsive ── */
    @media (max-width: 768px) {
      .auth-layout { grid-template-columns: 1fr; }
      .brand-panel { display: none; }
    }
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
        this.error.set('Credenciales incorrectas. Verifica tu correo y contrasena.');
        this.loading.set(false);
      },
    });
  }
}
