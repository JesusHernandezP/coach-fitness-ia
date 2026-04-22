import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProfileService, MetabolicProfile, NutritionTarget } from './profile.service';
import { AuthService } from '../../core/auth/auth.service';
import { DashboardService } from '../dashboard/dashboard.service';
import { DecimalPipe, DatePipe } from '@angular/common';

const ACTIVITY_OPTS: { value: MetabolicProfile['activityLevel']; label: string; hint: string }[] = [
  { value: 'SEDENTARY',         label: 'Sedentario',  hint: '< 1x/sem' },
  { value: 'LIGHTLY_ACTIVE',    label: 'Ligero',      hint: '1–3x/sem' },
  { value: 'MODERATELY_ACTIVE', label: 'Moderado',    hint: '3–5x/sem' },
  { value: 'VERY_ACTIVE',       label: 'Activo',      hint: '6–7x/sem' },
  { value: 'EXTRA_ACTIVE',      label: 'Intenso',     hint: '2x/día'   },
];

const GOAL_OPTS: { value: MetabolicProfile['goal']; label: string; icon: string }[] = [
  { value: 'LOSE',     label: 'Perder',   icon: '↓' },
  { value: 'MAINTAIN', label: 'Mantener', icon: '→' },
  { value: 'GAIN',     label: 'Ganar',    icon: '↑' },
];

const DIET_OPTS: { value: MetabolicProfile['dietType']; label: string }[] = [
  { value: 'STANDARD',             label: 'Estándar' },
  { value: 'KETO',                 label: 'Keto'     },
  { value: 'VEGETARIAN',           label: 'Veggie'   },
  { value: 'INTERMITTENT_FASTING', label: 'IF'       },
];

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule, DecimalPipe, DatePipe],
  template: `
    <div class="profile-page">
      <!-- Page header -->
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Mi cuenta</p>
          <h1 class="page-title">Perfil &amp; Objetivos</h1>
        </div>
      </header>

      <div class="profile-grid">
        <!-- ── LEFT: Form ── -->
        <section class="form-section">

          <div class="card form-card" style="--anim-delay:0s">
            <h2 class="card-heading"><span class="card-heading-line"></span>Datos personales</h2>

            @if (saveSuccess()) {
              <div class="toast-success">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5"/><path d="M4.5 7l2 2 3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Perfil guardado y macros recalculados
              </div>
            }
            @if (saveError()) {
              <div class="toast-error">{{ saveError() }}</div>
            }

            @if (loadingProfile()) {
              <div class="profile-skeleton">
                <div class="skel skel-row"></div>
                <div class="skel skel-row skel-short"></div>
              </div>
            } @else {
            <div class="form-row">
              <div class="field">
                <label class="label">Edad</label>
                <input class="input" type="number" [(ngModel)]="form.age" name="age" min="10" max="100" placeholder="30" />
              </div>
              <div class="field">
                <label class="label">Altura (cm)</label>
                <input class="input" type="number" [(ngModel)]="form.heightCm" name="heightCm" min="100" max="250" placeholder="170" />
              </div>
              <div class="field">
                <label class="label">Peso actual (kg)</label>
                <input class="input" type="number" [(ngModel)]="form.currentWeightKg" name="weight" min="30" max="300" step="0.1" placeholder="70" />
              </div>
            </div>

            <div class="field">
              <label class="label">Sexo</label>
              <div class="seg-group">
                @for (opt of sexOpts; track opt.value) {
                  <button type="button" class="seg-btn" [class.active]="form.sex === opt.value" (click)="form.sex = opt.value">
                    {{ opt.label }}
                  </button>
                }
              </div>
            </div>
            } <!-- end @else loadingProfile -->
          </div>

          <div class="card form-card" style="--anim-delay:0.08s">
            <h2 class="card-heading"><span class="card-heading-line"></span>Objetivo &amp; Actividad</h2>

            <div class="field">
              <label class="label">Objetivo</label>
              <div class="seg-group goal-group">
                @for (opt of goalOpts; track opt.value) {
                  <button type="button" class="seg-btn goal-btn" [class.active]="form.goal === opt.value" (click)="form.goal = opt.value">
                    <span class="goal-icon">{{ opt.icon }}</span>
                    {{ opt.label }}
                  </button>
                }
              </div>
            </div>

            <div class="field">
              <label class="label">Nivel de actividad</label>
              <div class="seg-group activity-group">
                @for (opt of activityOpts; track opt.value) {
                  <button type="button" class="seg-btn activity-btn" [class.active]="form.activityLevel === opt.value" (click)="form.activityLevel = opt.value">
                    <span class="act-label">{{ opt.label }}</span>
                    <span class="act-hint">{{ opt.hint }}</span>
                  </button>
                }
              </div>
            </div>

            <div class="field">
              <label class="label">Tipo de dieta</label>
              <div class="seg-group">
                @for (opt of dietOpts; track opt.value) {
                  <button type="button" class="seg-btn" [class.active]="form.dietType === opt.value" (click)="form.dietType = opt.value">
                    {{ opt.label }}
                  </button>
                }
              </div>
            </div>
          </div>

        </section>

        <!-- ── RIGHT: Macros ── -->
        <aside class="macros-section">
          <div class="card macros-card" style="--anim-delay:0.05s">
            <h2 class="card-heading"><span class="card-heading-line"></span>Macros calculados</h2>
            <p class="macros-sub">Objetivos diarios de ingesta</p>

            @if (targets()) {
              <div class="kcal-hero">
                <div class="kcal-ring-wrap">
                  <svg class="kcal-ring" viewBox="0 0 120 120">
                    <circle cx="60" cy="60" r="52" stroke="#1e1e1e" stroke-width="8" fill="none"/>
                    <circle cx="60" cy="60" r="52" stroke="url(#goldGrad)" stroke-width="8" fill="none"
                      stroke-linecap="round"
                      stroke-dasharray="326.7"
                      [attr.stroke-dashoffset]="kcalDashOffset()"
                      transform="rotate(-90 60 60)"
                      style="transition: stroke-dashoffset 1s cubic-bezier(.4,0,.2,1)"/>
                    <defs>
                      <linearGradient id="goldGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" stop-color="#d4b200"/>
                        <stop offset="100%" stop-color="#f0ca00"/>
                      </linearGradient>
                    </defs>
                  </svg>
                  <div class="kcal-inner">
                    <span class="kcal-val">{{ targets()!.calories | number:'1.0-0' }}</span>
                    <span class="kcal-unit">kcal</span>
                  </div>
                </div>
                <p class="kcal-label">Calorías objetivo / día</p>
              </div>

              <div class="macro-bars">
                <div class="macro-bar-row" style="--bar-color:#4ade80;--bar-delay:0.1s">
                  <div class="macro-bar-header">
                    <div class="macro-bar-left"><span class="macro-dot"></span><span class="macro-name">Proteína</span></div>
                    <div class="macro-bar-right">
                      <span class="macro-val">{{ targets()!.proteinG | number:'1.0-0' }}</span>
                      <span class="macro-unit">g</span>
                    </div>
                  </div>
                  <div class="bar-track"><div class="bar-fill" [style.width]="proteinPct() + '%'"></div></div>
                </div>

                <div class="macro-bar-row" style="--bar-color:#60a5fa;--bar-delay:0.2s">
                  <div class="macro-bar-header">
                    <div class="macro-bar-left"><span class="macro-dot"></span><span class="macro-name">Carbohidratos</span></div>
                    <div class="macro-bar-right">
                      <span class="macro-val">{{ targets()!.carbsG | number:'1.0-0' }}</span>
                      <span class="macro-unit">g</span>
                    </div>
                  </div>
                  <div class="bar-track"><div class="bar-fill" [style.width]="carbsPct() + '%'"></div></div>
                </div>

                <div class="macro-bar-row" style="--bar-color:#fb923c;--bar-delay:0.3s">
                  <div class="macro-bar-header">
                    <div class="macro-bar-left"><span class="macro-dot"></span><span class="macro-name">Grasas</span></div>
                    <div class="macro-bar-right">
                      <span class="macro-val">{{ targets()!.fatG | number:'1.0-0' }}</span>
                      <span class="macro-unit">g</span>
                    </div>
                  </div>
                  <div class="bar-track"><div class="bar-fill" [style.width]="fatPct() + '%'"></div></div>
                </div>
              </div>

              <div class="macro-legend">
                <div class="legend-item" style="--c:#4ade80">
                  <span class="legend-dot"></span><span class="legend-label">P {{ proteinPct() | number:'1.0-0' }}%</span>
                </div>
                <div class="legend-item" style="--c:#60a5fa">
                  <span class="legend-dot"></span><span class="legend-label">C {{ carbsPct() | number:'1.0-0' }}%</span>
                </div>
                <div class="legend-item" style="--c:#fb923c">
                  <span class="legend-dot"></span><span class="legend-label">G {{ fatPct() | number:'1.0-0' }}%</span>
                </div>
              </div>

              @if (targets()!.calculatedAt) {
                <p class="targets-ts">Calculado {{ targets()!.calculatedAt | date:'d MMM yyyy, HH:mm' }}</p>
              }
            } @else if (loadingTargets()) {
              <div class="macros-placeholder">
                <div class="pulse-ring"></div>
                <p>Cargando objetivos...</p>
              </div>
            } @else {
              <div class="macros-empty">
                <p>Guarda tu perfil para ver<br>los macros calculados</p>
                <span class="empty-arrow">↑</span>
              </div>
            }
          </div>

          @if (targets()) {
            <div class="info-pill">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.3"/><path d="M7 5v4M7 4h.01" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
              Mifflin-St Jeor · TDEE ajustado por actividad y objetivo
            </div>
          }
        </aside>

        <div class="actions-bar">
          <button class="btn btn-primary btn-save" (click)="save()" [disabled]="saving()">
            @if (saving()) {
              <span class="spinner"></span> Guardando...
            } @else {
              Guardar perfil
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 7h8M7 3l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            }
          </button>
          <button class="btn btn-danger btn-logout" (click)="logout()">Cerrar sesión</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .profile-page {
      max-width: 1100px;
      animation: fadeIn 0.4s ease both;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(8px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    .page-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      margin-bottom: 2rem;
    }

    .page-eyebrow {
      font-size: 0.68rem;
      font-weight: 500;
      letter-spacing: 0.15em;
      text-transform: uppercase;
      color: var(--accent);
      margin-bottom: 0.3rem;
    }

    .page-title {
      font-family: 'Bebas Neue', sans-serif;
      font-size: 2.4rem;
      letter-spacing: 0.04em;
      color: #f0f0f0;
    }

    .profile-grid {
      display: grid;
      grid-template-columns: 1fr 360px;
      gap: 1.5rem;
      align-items: start;
    }

    .form-section { display: flex; flex-direction: column; gap: 1.25rem; }

    .form-card { animation: slideUp 0.5s var(--anim-delay, 0s) ease both; }

    @keyframes slideUp {
      from { opacity: 0; transform: translateY(12px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    .profile-skeleton { display: flex; flex-direction: column; gap: 0.75rem; padding: 0.5rem 0 1rem; }
    .skel { background: linear-gradient(90deg, #1e1e1e 25%, #2a2a2a 50%, #1e1e1e 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 6px; height: 40px; }
    .skel-row { width: 100%; }
    .skel-short { width: 55%; height: 36px; }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

    .card-heading {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #888;
      margin-bottom: 1.25rem;
    }

    .card-heading-line {
      display: inline-block;
      width: 20px;
      height: 2px;
      background: var(--accent);
      border-radius: 1px;
      flex-shrink: 0;
    }

    .optional-badge {
      font-size: 0.65rem;
      background: rgba(212,178,0,0.12);
      color: var(--accent);
      border: 1px solid rgba(212,178,0,0.2);
      border-radius: 100px;
      padding: 0.1rem 0.5rem;
      font-weight: 500;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      margin-left: 0.5rem;
    }

    .form-row {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1rem;
      margin-bottom: 1.25rem;
    }

    .field {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      margin-bottom: 1.1rem;
    }
    .field:last-child { margin-bottom: 0; }

    /* Segmented buttons */
    .seg-group { display: flex; gap: 0.4rem; flex-wrap: wrap; }

    .seg-btn {
      flex: 1;
      padding: 0.5rem 0.75rem;
      background: var(--surface2);
      border: 1px solid var(--border);
      border-radius: 8px;
      color: var(--text-muted);
      font-family: 'DM Sans', sans-serif;
      font-size: 0.8rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.15s ease;
      text-align: center;
      white-space: nowrap;
    }
    .seg-btn:hover:not(.active) { border-color: #444; color: var(--text); }
    .seg-btn.active {
      background: rgba(212,178,0,0.12);
      border-color: var(--accent);
      color: var(--accent);
      font-weight: 600;
    }

    .goal-group { gap: 0.5rem; }
    .goal-btn { display: flex; flex-direction: column; align-items: center; gap: 0.15rem; padding: 0.6rem 1rem; }
    .goal-icon { font-size: 1.1rem; line-height: 1; }

    .activity-group { gap: 0.4rem; }
    .activity-btn { display: flex; flex-direction: column; align-items: center; gap: 0.1rem; padding: 0.5rem; }
    .act-label { font-size: 0.78rem; font-weight: 600; }
    .act-hint  { font-size: 0.65rem; opacity: 0.6; }

    .actions-bar {
      display: flex;
      gap: 1rem;
      grid-column: 1;
      align-self: start;
    }
    .actions-bar .btn {
      flex: 1;
      justify-content: center;
      min-width: 0;
    }
    .btn-logout {
      padding: 0.8rem 2rem;
      font-size: 0.9rem;
      border-radius: 10px;
    }

    .btn-save {
      padding: 0.8rem 2rem;
      font-size: 0.9rem;
      border-radius: 10px;
      width: fit-content;
      transition: background 0.2s, transform 0.1s, box-shadow 0.2s;
    }
    .btn-save:hover:not(:disabled) {
      box-shadow: 0 0 20px rgba(212,178,0,0.3);
      transform: translateY(-1px);
    }

    .spinner {
      width: 13px; height: 13px;
      border: 2px solid rgba(0,0,0,0.2);
      border-top-color: #000;
      border-radius: 50%;
      display: inline-block;
      animation: spin 0.7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .toast-success, .toast-error {
      display: flex; align-items: center; gap: 0.5rem;
      border-radius: 8px; font-size: 0.82rem;
      padding: 0.6rem 0.9rem; margin-bottom: 1.1rem;
    }
    .toast-success { background: rgba(74,222,128,0.08); border: 1px solid rgba(74,222,128,0.2); color: #4ade80; }
    .toast-error   { background: rgba(224,82,82,0.08);  border: 1px solid rgba(224,82,82,0.2);  color: #e05252; }

    /* Macros panel */
    .macros-section { position: sticky; top: 0; display: flex; flex-direction: column; gap: 0.75rem; }
    .macros-card { animation: slideUp 0.5s var(--anim-delay, 0s) ease both; }
    .macros-sub { font-size: 0.78rem; color: #555; margin-top: -0.75rem; margin-bottom: 1.5rem; }

    .kcal-hero { display: flex; flex-direction: column; align-items: center; margin-bottom: 1.75rem; }
    .kcal-ring-wrap { position: relative; width: 120px; height: 120px; margin-bottom: 0.75rem; }
    .kcal-ring { width: 120px; height: 120px; }
    .kcal-inner { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; }
    .kcal-val { font-family: 'Bebas Neue', sans-serif; font-size: 1.9rem; color: var(--accent); line-height: 1; }
    .kcal-unit { font-size: 0.65rem; color: #666; text-transform: uppercase; letter-spacing: 0.1em; }
    .kcal-label { font-size: 0.75rem; color: #666; text-align: center; }

    .macro-bars { display: flex; flex-direction: column; gap: 1.1rem; margin-bottom: 1.25rem; }
    .macro-bar-row { animation: slideUp 0.5s var(--bar-delay, 0s) ease both; }
    .macro-bar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; }
    .macro-bar-left { display: flex; align-items: center; gap: 0.5rem; }
    .macro-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--bar-color); box-shadow: 0 0 6px var(--bar-color); }
    .macro-name { font-size: 0.8rem; color: #aaa; }
    .macro-bar-right { display: flex; align-items: baseline; gap: 0.2rem; }
    .macro-val { font-family: 'Bebas Neue', sans-serif; font-size: 1.3rem; color: var(--text); }
    .macro-unit { font-size: 0.68rem; color: #666; }
    .bar-track { height: 4px; background: #1e1e1e; border-radius: 2px; overflow: hidden; }
    .bar-fill {
      height: 100%; background: var(--bar-color); border-radius: 2px;
      transition: width 0.9s cubic-bezier(.4,0,.2,1);
      box-shadow: 0 0 8px var(--bar-color);
    }

    .macro-legend { display: flex; justify-content: center; gap: 1.25rem; padding-top: 0.75rem; border-top: 1px solid var(--border); margin-bottom: 0.75rem; }
    .legend-item { display: flex; align-items: center; gap: 0.35rem; }
    .legend-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--c); }
    .legend-label { font-size: 0.72rem; color: #777; }

    .targets-ts { font-size: 0.68rem; color: #444; text-align: center; }

    .macros-empty { text-align: center; padding: 2rem 1rem; color: #555; font-size: 0.85rem; line-height: 1.6; }
    .empty-arrow { font-size: 1.5rem; color: var(--accent); display: block; margin-top: 0.5rem; }

    .macros-placeholder { display: flex; flex-direction: column; align-items: center; gap: 1rem; padding: 2rem; color: #555; font-size: 0.82rem; }
    .pulse-ring { width: 40px; height: 40px; border-radius: 50%; border: 2px solid var(--accent); animation: pulse 1.2s ease-in-out infinite; }
    @keyframes pulse {
      0%, 100% { opacity: 0.3; transform: scale(0.9); }
      50%       { opacity: 1;   transform: scale(1.1); }
    }

    .info-pill {
      display: flex; align-items: center; gap: 0.5rem;
      background: var(--surface); border: 1px solid var(--border);
      border-radius: 100px; padding: 0.45rem 0.9rem;
      font-size: 0.7rem; color: #555;
      animation: fadeIn 0.4s 0.3s ease both;
    }

    @media (max-width: 900px) {
      .profile-grid { grid-template-columns: 1fr; }
      .macros-section { position: static; order: 2; }
      .form-section   { order: 1; }
      .actions-bar    { order: 3; grid-column: 1; }
      .form-row { grid-template-columns: repeat(2, 1fr); }
    }
  `],
})
export class ProfileComponent implements OnInit {
  private profileSvc = inject(ProfileService);
  private dashSvc = inject(DashboardService);
  private auth = inject(AuthService);

  private originalWeightKg = 0;

  activityOpts = ACTIVITY_OPTS;
  goalOpts = GOAL_OPTS;
  dietOpts = DIET_OPTS;
  sexOpts: { value: MetabolicProfile['sex']; label: string }[] = [
    { value: 'MALE',   label: 'Hombre' },
    { value: 'FEMALE', label: 'Mujer'  },
  ];

  form: MetabolicProfile = {
    age: 0,
    sex: 'MALE',
    heightCm: 0,
    currentWeightKg: 0,
    activityLevel: 'MODERATELY_ACTIVE',
    goal: 'MAINTAIN',
    dietType: 'STANDARD',
  };

  targets = signal<NutritionTarget | null>(null);
  saving = signal(false);
  saveSuccess = signal(false);
  saveError = signal('');
  loadingTargets = signal(true);
  loadingProfile = signal(true);

  readonly KCAL_CIRCUMFERENCE = 2 * Math.PI * 52;

  kcalDashOffset = computed(() => {
    const t = this.targets();
    if (!t) return this.KCAL_CIRCUMFERENCE;
    const pct = Math.min(t.calories / 3500, 1);
    return this.KCAL_CIRCUMFERENCE * (1 - pct);
  });

  totalMacroKcal = computed(() => {
    const t = this.targets();
    if (!t) return 1;
    return t.proteinG * 4 + t.carbsG * 4 + t.fatG * 9;
  });

  proteinPct = computed(() => {
    const t = this.targets();
    if (!t) return 0;
    return Math.round((t.proteinG * 4 / this.totalMacroKcal()) * 100);
  });

  carbsPct = computed(() => {
    const t = this.targets();
    if (!t) return 0;
    return Math.round((t.carbsG * 4 / this.totalMacroKcal()) * 100);
  });

  fatPct = computed(() => {
    const t = this.targets();
    if (!t) return 0;
    return Math.round((t.fatG * 9 / this.totalMacroKcal()) * 100);
  });

  ngOnInit() {
    this.loadProfile();
    this.profileSvc.getTargets().subscribe({
      next: t => { this.targets.set(t); this.loadingTargets.set(false); },
      error: () => this.loadingTargets.set(false),
    });
  }

  private loadProfile() {
    this.loadingProfile.set(true);
    this.profileSvc.getProfile().subscribe({
      next: p => {
        Object.assign(this.form, p);
        this.originalWeightKg = p.currentWeightKg;
        this.loadingProfile.set(false);
      },
      error: () => this.loadingProfile.set(false),
    });
  }

  save() {
    this.saving.set(true);
    this.saveSuccess.set(false);
    this.saveError.set('');

    const weightChanged = this.form.currentWeightKg !== this.originalWeightKg;
    this.profileSvc.saveProfile(this.form).subscribe({
      next: saved => {
        Object.assign(this.form, saved);
        this.originalWeightKg = saved.currentWeightKg;
        this.profileSvc.getTargets().subscribe({
          next: t => this.targets.set(t),
          error: () => {},
        });
        if (weightChanged && saved.currentWeightKg) {
          this.dashSvc.addWeight({ weightKg: saved.currentWeightKg }).subscribe({ error: () => {} });
        }
        this.saving.set(false);
        this.saveSuccess.set(true);
        setTimeout(() => this.saveSuccess.set(false), 4000);
      },
      error: err => {
        this.saveError.set(err.error?.message ?? 'Error al guardar perfil');
        this.saving.set(false);
      },
    });
  }

  logout() {
    this.auth.logout();
  }
}
