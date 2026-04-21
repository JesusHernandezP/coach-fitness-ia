import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="auth-layout">
      <!-- Left panel: branding -->
      <aside class="brand-panel">
        <div class="brand-orb orb-1"></div>
        <div class="brand-orb orb-2"></div>

        <div class="brand-content">
          <span class="brand-eyebrow">Empieza hoy</span>
          <h1 class="brand-title">CREA TU<br>PERFIL<br>FITNESS</h1>
          <p class="brand-sub">Gratis · Sin tarjeta · Sin excusas</p>

          <ul class="brand-steps">
            <li><span class="step-num">01</span><span>Crea tu cuenta</span></li>
            <li><span class="step-num">02</span><span>Completa tu perfil metabolico</span></li>
            <li><span class="step-num">03</span><span>Obtén tus macros personalizados</span></li>
            <li><span class="step-num">04</span><span>Chatea con tu coach IA</span></li>
          </ul>
        </div>

        <div class="brand-badge">
          <span class="badge-icon">✦</span>
          <span class="badge-text">Potenciado por Groq · Llama 3.3 70B</span>
        </div>
      </aside>

      <!-- Right panel: form -->
      <main class="form-panel">
        <div class="form-card">
          <div class="form-header">
            <p class="form-eyebrow">Nuevo usuario</p>
            <h2 class="form-title">Crear cuenta</h2>
            <p class="form-hint">Tarda menos de un minuto</p>
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
              <label class="label" for="password">Contrasena <span class="label-hint">(min. 8 caracteres)</span></label>
              <div class="input-wrap">
                <svg class="input-icon" width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" stroke-width="1.3"/><path d="M5 7V5a3 3 0 0 1 6 0v2" stroke="currentColor" stroke-width="1.3"/></svg>
                <input class="input has-icon" id="password" type="password" [(ngModel)]="password" name="password" placeholder="••••••••" required minlength="8" autocomplete="new-password" />
              </div>
              @if (password.length > 0 && password.length < 8) {
                <span class="field-hint">Minimo 8 caracteres</span>
              }
            </div>

            <div class="password-strength" [class.visible]="password.length >= 8">
              <div class="strength-bar">
                <div class="strength-fill" [style.width]="strengthWidth()"></div>
              </div>
              <span class="strength-label">{{ strengthLabel() }}</span>
            </div>

            <button class="btn btn-primary btn-submit" type="submit" [disabled]="loading() || password.length < 8">
              @if (loading()) {
                <span class="spinner"></span> Creando cuenta...
              } @else {
                Crear cuenta
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 7h8M7 3l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              }
            </button>
          </form>

          <p class="form-switch">
            ¿Ya tienes cuenta?
            <a routerLink="/login" class="form-link">Iniciar sesion</a>
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
    .orb-1 { width: 300px; height: 300px; background: radial-gradient(circle, rgba(212,178,0,0.15) 0%, transparent 70%); top: -40px; left: -40px; animation-delay: 0s; }
    .orb-2 { width: 200px; height: 200px; background: radial-gradient(circle, rgba(212,178,0,0.10) 0%, transparent 70%); bottom: 80px; right: 20px; animation-delay: -4s; }

    @keyframes float {
      0%, 100% { transform: translateY(0); }
      50% { transform: translateY(-16px); }
    }

    .brand-content { position: relative; z-index: 1; animation: fadeSlideUp 0.7s ease both; }

    .brand-eyebrow {
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
      font-size: clamp(3rem, 5.5vw, 5rem);
      line-height: 0.92;
      letter-spacing: 0.02em;
      color: #f0f0f0;
      margin-bottom: 1rem;
    }

    .brand-sub {
      font-size: 0.78rem;
      color: #555;
      letter-spacing: 0.15em;
      text-transform: uppercase;
      margin-bottom: 2.5rem;
    }

    .brand-steps {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 1.1rem;
    }

    .brand-steps li {
      display: flex;
      align-items: center;
      gap: 1rem;
      font-size: 0.875rem;
      color: #888;
    }

    .step-num {
      font-family: 'Bebas Neue', sans-serif;
      font-size: 1.1rem;
      color: var(--accent);
      opacity: 0.8;
      min-width: 24px;
    }

    .brand-badge {
      position: relative;
      z-index: 1;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: rgba(212,178,0,0.07);
      border: 1px solid rgba(212,178,0,0.2);
      border-radius: 100px;
      padding: 0.45rem 1rem;
      animation: fadeSlideUp 0.7s 0.15s ease both;
      width: fit-content;
    }
    .badge-icon { color: var(--accent); font-size: 0.7rem; }
    .badge-text { font-size: 0.72rem; color: #888; letter-spacing: 0.05em; }

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

    .label-hint { font-weight: 400; opacity: 0.6; margin-left: 0.25rem; }
    .field-hint { font-size: 0.73rem; color: #e05252; margin-top: 0.35rem; }

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

    .password-strength {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      opacity: 0;
      transition: opacity 0.3s;
      margin-top: -0.5rem;
    }
    .password-strength.visible { opacity: 1; }

    .strength-bar {
      flex: 1;
      height: 3px;
      background: #2e2e2e;
      border-radius: 2px;
      overflow: hidden;
    }
    .strength-fill {
      height: 100%;
      background: var(--accent);
      border-radius: 2px;
      transition: width 0.3s ease;
    }
    .strength-label { font-size: 0.72rem; color: #666; white-space: nowrap; }

    .btn-submit {
      width: 100%;
      padding: 0.75rem;
      font-size: 0.9rem;
      border-radius: 10px;
      margin-top: 0.25rem;
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

    @media (max-width: 768px) {
      .auth-layout { grid-template-columns: 1fr; }
      .brand-panel { display: none; }
    }
  `],
})
export class RegisterComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  loading = signal(false);
  error = signal('');

  strengthWidth() {
    const len = this.password.length;
    if (len === 0) return '0%';
    if (len < 8) return '25%';
    if (len < 12) return '55%';
    if (len < 16) return '80%';
    return '100%';
  }

  strengthLabel() {
    const len = this.password.length;
    if (len < 8) return '';
    if (len < 12) return 'Aceptable';
    if (len < 16) return 'Buena';
    return 'Excelente';
  }

  submit() {
    this.loading.set(true);
    this.error.set('');
    this.auth.register(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/profile']),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Error al registrar. Intenta de nuevo.');
        this.loading.set(false);
      },
    });
  }
}
