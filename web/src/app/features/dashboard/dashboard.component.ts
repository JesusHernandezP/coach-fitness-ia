import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, DatePipe } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import {
  DashboardService,
  WeightPoint,
  WeeklySummary,
  TodaySnapshot,
  ActivityEntry,
} from './dashboard.service';
import { NutritionService, DailyNutritionSummary, FoodLog } from './nutrition.service';

const today = () => new Date().toISOString().slice(0, 10);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule, DecimalPipe, DatePipe, BaseChartDirective],
  template: `
    <div class="dash-page">
      <!-- ── Header ── -->
      <header class="dash-header">
        <div>
          <p class="page-eyebrow">Vista general</p>
          <h1 class="page-title">Panel de Control</h1>
        </div>
        <p class="dash-date">{{ now | date:'EEEE, d MMMM yyyy' }}</p>
      </header>

      <!-- ── Stat cards ── -->
      <div class="stat-grid">
        <div class="stat-card" style="--delay:0s">
          <span class="stat-label">Pasos hoy</span>
          <span class="stat-value">{{ today()?.steps ?? 0 | number }}</span>
          <span class="stat-sub">objetivo 8 000</span>
          <div class="stat-bar">
            <div class="stat-bar-fill steps-fill" [style.width]="stepsPct() + '%'"></div>
          </div>
        </div>

        <div class="stat-card" style="--delay:0.06s">
          <span class="stat-label">Cal. quemadas</span>
          <span class="stat-value accent">{{ today()?.caloriesBurned ?? 0 | number }}</span>
          <span class="stat-sub">kcal activas</span>
          <div class="stat-ring-wrap">
            <svg viewBox="0 0 36 36" class="stat-ring-svg">
              <circle cx="18" cy="18" r="15.9" fill="none" stroke="#1e1e1e" stroke-width="3"/>
              <circle cx="18" cy="18" r="15.9" fill="none" stroke="#d4b200" stroke-width="3"
                stroke-linecap="round"
                [attr.stroke-dasharray]="'100 100'"
                [attr.stroke-dashoffset]="100 - calBurnedPct()"
                transform="rotate(-90 18 18)"
                style="transition: stroke-dashoffset 1s ease"/>
            </svg>
          </div>
        </div>

        <div class="stat-card" style="--delay:0.12s">
          <span class="stat-label">Días activos</span>
          <span class="stat-value">{{ weekly()?.daysLogged ?? 0 }}<span class="stat-denom">/7</span></span>
          <span class="stat-sub">esta semana</span>
          <div class="day-dots">
            @for (i of [0,1,2,3,4,5,6]; track i) {
              <span class="day-dot" [class.lit]="i < (weekly()?.daysLogged ?? 0)"></span>
            }
          </div>
        </div>

        <div class="stat-card" style="--delay:0.18s">
          <span class="stat-label">Delta peso 7d</span>
          @if (weekly()?.weightDelta !== null && weekly()?.weightDelta !== undefined) {
            <span class="stat-value" [class.positive]="(weekly()?.weightDelta ?? 0) > 0" [class.negative]="(weekly()?.weightDelta ?? 0) < 0">
              {{ (weekly()?.weightDelta ?? 0) > 0 ? '+' : '' }}{{ weekly()?.weightDelta | number:'1.1-1' }} kg
            </span>
          } @else {
            <span class="stat-value muted">—</span>
          }
          <span class="stat-sub">últimos 7 días</span>
          <div class="delta-arrow" [class.up]="(weekly()?.weightDelta ?? 0) > 0" [class.down]="(weekly()?.weightDelta ?? 0) < 0">
            {{ (weekly()?.weightDelta ?? 0) > 0 ? '↑' : (weekly()?.weightDelta ?? 0) < 0 ? '↓' : '→' }}
          </div>
        </div>
      </div>

      <!-- ── Main grid ── -->
      <div class="main-grid">
        <!-- Weight chart card -->
        <div class="card chart-card weight-card" style="--delay:0.08s">
          <div class="chart-header">
            <div>
              <h2 class="chart-title">Progreso de Peso</h2>
              <p class="chart-sub">Últimos 90 días</p>
            </div>
            <div class="add-weight-inline">
              <input class="input input-sm" type="number" [(ngModel)]="newWeight" placeholder="kg" step="0.1" min="30" max="300" />
              <button class="btn btn-primary btn-sm" (click)="addWeight()" [disabled]="addingWeight() || !newWeight">
                @if (addingWeight()) { <span class="spinner-sm"></span> } @else { + Peso }
              </button>
            </div>
          </div>

          @if (weightData().labels!.length > 0) {
            <div class="chart-wrap">
              <canvas baseChart
                [data]="weightData()"
                [options]="lineOpts"
                type="line">
              </canvas>
            </div>
          } @else {
            <div class="chart-empty">
              <span class="empty-icon">⚖</span>
              <p>Aún no hay registros de peso.<br>Añade el primero arriba.</p>
            </div>
          }
        </div>

        <!-- Weekly steps chart -->
        <div class="card chart-card steps-card" style="--delay:0.16s">
          <div class="chart-header">
            <div>
              <h2 class="chart-title">Pasos Semanales</h2>
              <p class="chart-sub">Últimos 7 días</p>
            </div>
            @if (weekly()) {
              <div class="weekly-avg">
                <span class="weekly-avg-val">{{ weekly()!.avgSteps | number:'1.0-0' }}</span>
                <span class="weekly-avg-lbl">prom/día</span>
              </div>
            }
          </div>

          @if (stepsData().labels!.length > 0) {
            <div class="chart-wrap chart-wrap-sm">
              <canvas baseChart
                [data]="stepsData()"
                [options]="barOpts"
                type="bar">
              </canvas>
            </div>
          } @else {
            <div class="chart-empty chart-empty-sm">
              <span class="empty-icon">🦶</span>
              <p>Sin actividad registrada esta semana.</p>
            </div>
          }
        </div>

        <!-- Log activity form -->
        <div class="card activity-card" style="--delay:0.22s">
          <h2 class="chart-title">Registrar Actividad</h2>
          <p class="chart-sub">Upsert por fecha</p>

          @if (activitySaved()) {
            <div class="toast-success">
              <svg width="13" height="13" viewBox="0 0 14 14" fill="none"><circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5"/><path d="M4.5 7l2 2 3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              Actividad guardada
            </div>
          }

          <div class="activity-form">
            <div class="field-row">
              <div class="field">
                <label class="label">Fecha</label>
                <input class="input input-sm" type="date" [(ngModel)]="actForm.date" name="actDate" />
              </div>
              <div class="field">
                <label class="label">Pasos</label>
                <input class="input input-sm" type="number" [(ngModel)]="actForm.steps" name="actSteps" min="0" max="100000" placeholder="0" />
              </div>
            </div>
            <div class="field-row">
              <div class="field">
                <label class="label">Cal. quemadas</label>
                <input class="input input-sm" type="number" [(ngModel)]="actForm.caloriesBurned" name="actCal" min="0" placeholder="0" />
              </div>
              <div class="field">
                <label class="label">Notas</label>
                <input class="input input-sm" type="text" [(ngModel)]="actForm.notes" name="actNotes" placeholder="Carrera, gym..." />
              </div>
            </div>
            <button class="btn btn-primary btn-full" (click)="logActivity()" [disabled]="savingActivity()">
              @if (savingActivity()) { <span class="spinner-sm"></span> Guardando... } @else { Guardar actividad }
            </button>
          </div>
        </div>
      </div>

      <div class="nutrition-grid">
        <section class="card nutrition-summary-card" style="--delay:0.24s">
          <div class="chart-header">
            <div>
              <h2 class="chart-title">Resumen Nutricional</h2>
              <p class="chart-sub">Consumo de hoy frente a tus objetivos</p>
            </div>
          </div>

          @if (dailyNutrition()) {
            <div class="nutrition-hero">
              <div>
                <span class="stat-label">Consumidas</span>
                <span class="nutrition-main">{{ dailyNutrition()!.consumedCalories | number:'1.0-0' }}</span>
              </div>
              <div class="nutrition-chip" [class.excess]="(dailyNutrition()!.remainingCalories ?? 0) < 0">
                Restan {{ dailyNutrition()!.remainingCalories ?? 0 | number:'1.0-0' }} kcal
              </div>
            </div>

            <div class="nutrition-macro-grid">
              <div class="nutrition-macro-card">
                <span class="macro-name">Proteína</span>
                <strong>{{ dailyNutrition()!.consumedProteinG | number:'1.0-0' }}g</strong>
                <span class="macro-hint">restan {{ dailyNutrition()!.remainingProteinG ?? 0 | number:'1.0-0' }}g</span>
              </div>
              <div class="nutrition-macro-card">
                <span class="macro-name">Carbs</span>
                <strong>{{ dailyNutrition()!.consumedCarbsG | number:'1.0-0' }}g</strong>
                <span class="macro-hint">restan {{ dailyNutrition()!.remainingCarbsG ?? 0 | number:'1.0-0' }}g</span>
              </div>
              <div class="nutrition-macro-card">
                <span class="macro-name">Grasa</span>
                <strong>{{ dailyNutrition()!.consumedFatG | number:'1.0-0' }}g</strong>
                <span class="macro-hint">restan {{ dailyNutrition()!.remainingFatG ?? 0 | number:'1.0-0' }}g</span>
              </div>
            </div>
          } @else {
            <div class="chart-empty chart-empty-sm">
              <span class="empty-icon">🍽</span>
              <p>Sin resumen nutricional aún.</p>
            </div>
          }
        </section>

        <section class="card nutrition-log-card" style="--delay:0.28s">
          <div class="chart-header">
            <div>
              <h2 class="chart-title">Comidas de Hoy</h2>
              <p class="chart-sub">Alta manual simple</p>
            </div>
          </div>

          <div class="food-form">
            <div class="field-row">
              <div class="field">
                <label class="label">Tipo</label>
                <select class="input input-sm" [(ngModel)]="foodForm.mealType" name="mealType">
                  @for (option of mealTypeOptions; track option.value) {
                    <option [value]="option.value">{{ option.label }}</option>
                  }
                </select>
              </div>
              <div class="field">
                <label class="label">Kcal</label>
                <input class="input input-sm" type="number" [(ngModel)]="foodForm.calories" name="foodCalories" min="0" placeholder="0" />
              </div>
            </div>

            <div class="field">
              <label class="label">Descripción</label>
              <input class="input input-sm" type="text" [(ngModel)]="foodForm.description" name="foodDescription" placeholder="Ej. pechuga con arroz" />
            </div>

            <div class="field-row">
              <div class="field">
                <label class="label">Proteína</label>
                <input class="input input-sm" type="number" [(ngModel)]="foodForm.proteinG" name="foodProtein" min="0" placeholder="0" />
              </div>
              <div class="field">
                <label class="label">Carbs</label>
                <input class="input input-sm" type="number" [(ngModel)]="foodForm.carbsG" name="foodCarbs" min="0" placeholder="0" />
              </div>
            </div>

            <div class="field">
              <label class="label">Grasa</label>
              <input class="input input-sm" type="number" [(ngModel)]="foodForm.fatG" name="foodFat" min="0" placeholder="0" />
            </div>

            <button class="btn btn-primary btn-full" (click)="addFoodLog()" [disabled]="savingFood() || !foodForm.description.trim() || foodForm.calories === null">
              @if (savingFood()) { <span class="spinner-sm"></span> Guardando... } @else { Guardar comida }
            </button>
          </div>

          @if (todayFoodLogs().length > 0) {
            <div class="food-list">
              @for (food of todayFoodLogs(); track food.id) {
                <div class="food-row">
                  <div>
                    <p class="food-title">{{ food.description }}</p>
                    <p class="food-meta">{{ food.mealType }} · P {{ food.proteinG ?? 0 | number:'1.0-0' }} · C {{ food.carbsG ?? 0 | number:'1.0-0' }} · G {{ food.fatG ?? 0 | number:'1.0-0' }}</p>
                  </div>
                  <strong class="food-calories">{{ food.calories | number:'1.0-0' }} kcal</strong>
                </div>
              }
            </div>
          } @else {
            <div class="chart-empty chart-empty-sm">
              <span class="empty-icon">🥗</span>
              <p>Aún no has registrado comidas hoy.</p>
            </div>
          }
        </section>
      </div>
    </div>
  `,
  styles: [`
    .dash-page {
      max-width: 1200px;
      animation: fadeIn 0.4s ease both;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(8px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    /* Background dot-grid on the page-level shell — applied via body */
    :host {
      display: block;
    }

    /* ── Header ── */
    .dash-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      margin-bottom: 1.75rem;
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

    .dash-date {
      font-size: 0.78rem;
      color: #555;
      text-transform: capitalize;
    }

    /* ── Stat cards ── */
    .stat-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .stat-card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 1.25rem;
      position: relative;
      overflow: hidden;
      animation: slideUp 0.5s var(--delay, 0s) ease both;
      transition: border-color 0.2s;

      &::before {
        content: '';
        position: absolute;
        inset: 0;
        background: radial-gradient(ellipse at top right, rgba(212,178,0,0.04) 0%, transparent 60%);
        pointer-events: none;
      }

      &:hover { border-color: rgba(212,178,0,0.2); }
    }

    @keyframes slideUp {
      from { opacity: 0; transform: translateY(12px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    .stat-label {
      display: block;
      font-size: 0.68rem;
      font-weight: 600;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #666;
      margin-bottom: 0.5rem;
    }

    .stat-value {
      display: block;
      font-family: 'Bebas Neue', sans-serif;
      font-size: 2.4rem;
      line-height: 1;
      color: var(--text);
      margin-bottom: 0.25rem;

      &.accent { color: var(--accent); }
      &.positive { color: #e05252; }
      &.negative { color: #4ade80; }
      &.muted { color: #555; }
    }

    .stat-denom { font-size: 1.4rem; color: #444; }

    .stat-sub {
      display: block;
      font-size: 0.7rem;
      color: #555;
      margin-bottom: 0.75rem;
    }

    .stat-bar {
      height: 3px;
      background: #1e1e1e;
      border-radius: 2px;
      overflow: hidden;
    }

    .stat-bar-fill {
      height: 100%;
      border-radius: 2px;
      transition: width 1s cubic-bezier(.4,0,.2,1);
    }

    .steps-fill { background: linear-gradient(90deg, #4ade80, #22c55e); box-shadow: 0 0 8px rgba(74,222,128,0.4); }

    /* Calories ring */
    .stat-ring-wrap {
      position: absolute;
      bottom: 0.75rem;
      right: 0.75rem;
      width: 40px;
      height: 40px;
    }

    .stat-ring-svg { width: 40px; height: 40px; }

    /* Day dots */
    .day-dots { display: flex; gap: 5px; }
    .day-dot {
      width: 8px; height: 8px;
      border-radius: 50%;
      background: #2a2a2a;
      transition: background 0.3s, box-shadow 0.3s;

      &.lit {
        background: var(--accent);
        box-shadow: 0 0 6px rgba(212,178,0,0.5);
      }
    }

    /* Delta arrow */
    .delta-arrow {
      position: absolute;
      bottom: 1rem;
      right: 1.25rem;
      font-size: 1.5rem;
      color: #333;

      &.up { color: #e05252; }
      &.down { color: #4ade80; }
    }

    /* ── Main grid ── */
    .main-grid {
      display: grid;
      grid-template-columns: 1fr 380px;
      grid-template-rows: auto auto;
      gap: 1rem;
    }

    .nutrition-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-top: 1rem;
    }

    .chart-card {
      animation: slideUp 0.5s var(--delay, 0s) ease both;
    }

    .weight-card {
      grid-column: 1;
      grid-row: 1;
    }

    .steps-card {
      grid-column: 1;
      grid-row: 2;
    }

    .activity-card {
      grid-column: 2;
      grid-row: 1 / 3;
      animation: slideUp 0.5s var(--delay, 0s) ease both;
    }

    .chart-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 1.25rem;
    }

    .chart-title {
      font-family: 'Bebas Neue', sans-serif;
      font-size: 1.3rem;
      letter-spacing: 0.05em;
      color: #e0e0e0;
      margin-bottom: 0.15rem;
    }

    .chart-sub { font-size: 0.72rem; color: #555; }

    .chart-wrap {
      height: 220px;
      position: relative;
    }

    .chart-wrap-sm { height: 160px; }

    .chart-empty, .chart-empty-sm {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
      color: #555;
      font-size: 0.82rem;
      text-align: center;
      padding: 1.5rem;
    }

    .chart-empty { height: 200px; }
    .chart-empty-sm { height: 140px; }
    .empty-icon { font-size: 1.8rem; filter: grayscale(1) opacity(0.4); }

    /* ── Add weight inline ── */
    .add-weight-inline {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }

    .input-sm { padding: 0.45rem 0.7rem; font-size: 0.82rem; width: 80px; }
    .btn-sm { padding: 0.45rem 0.9rem; font-size: 0.8rem; white-space: nowrap; }
    .btn-full { width: 100%; margin-top: 0.5rem; }

    /* ── Weekly avg badge ── */
    .weekly-avg {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }

    .weekly-avg-val {
      font-family: 'Bebas Neue', sans-serif;
      font-size: 1.5rem;
      color: var(--accent);
      line-height: 1;
    }

    .weekly-avg-lbl { font-size: 0.65rem; color: #555; }

    /* ── Activity form ── */
    .activity-form { display: flex; flex-direction: column; gap: 0.9rem; margin-top: 1rem; }
    .food-form { display: flex; flex-direction: column; gap: 0.85rem; margin-bottom: 1rem; }
    .nutrition-hero {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .nutrition-main {
      display: block;
      font-family: 'Bebas Neue', sans-serif;
      font-size: 2.3rem;
      line-height: 1;
      color: var(--text);
    }
    .nutrition-chip {
      border-radius: 999px;
      padding: 0.4rem 0.8rem;
      background: rgba(74,222,128,0.08);
      border: 1px solid rgba(74,222,128,0.2);
      color: #4ade80;
      font-size: 0.78rem;
      white-space: nowrap;
    }
    .nutrition-chip.excess {
      background: rgba(224,82,82,0.08);
      border-color: rgba(224,82,82,0.2);
      color: #e05252;
    }
    .nutrition-macro-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.75rem;
    }
    .nutrition-macro-card {
      background: #121212;
      border: 1px solid #1f1f1f;
      border-radius: 12px;
      padding: 0.9rem;
      display: flex;
      flex-direction: column;
      gap: 0.2rem;
    }
    .macro-hint { color: #666; font-size: 0.7rem; }
    .food-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .food-row {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: center;
      padding: 0.85rem 0;
      border-top: 1px solid #1e1e1e;
    }
    .food-title { color: #eee; font-size: 0.84rem; margin-bottom: 0.18rem; }
    .food-meta { color: #666; font-size: 0.72rem; text-transform: capitalize; }
    .food-calories { color: var(--accent); font-size: 0.82rem; white-space: nowrap; }

    .field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }

    .field { display: flex; flex-direction: column; gap: 0.35rem; }

    /* ── Spinner ── */
    .spinner-sm {
      width: 12px; height: 12px;
      border: 2px solid rgba(0,0,0,0.2);
      border-top-color: #000;
      border-radius: 50%;
      display: inline-block;
      animation: spin 0.7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* ── Toast ── */
    .toast-success {
      display: flex; align-items: center; gap: 0.5rem;
      background: rgba(74,222,128,0.08);
      border: 1px solid rgba(74,222,128,0.2);
      border-radius: 8px; color: #4ade80;
      font-size: 0.8rem; padding: 0.55rem 0.8rem;
      margin-bottom: 0.5rem;
    }

    /* ── Responsive ── */
    @media (max-width: 1024px) {
      .stat-grid { grid-template-columns: repeat(2, 1fr); }
    }

    @media (max-width: 768px) {
      .main-grid { grid-template-columns: 1fr; }
      .nutrition-grid { grid-template-columns: 1fr; }
      .weight-card, .steps-card, .activity-card { grid-column: 1; grid-row: auto; }
      .stat-grid { grid-template-columns: repeat(2, 1fr); }
      .nutrition-macro-grid { grid-template-columns: 1fr; }
    }
  `],
})
export class DashboardComponent implements OnInit {
  private svc = inject(DashboardService);
  private nutritionSvc = inject(NutritionService);

  now = new Date();

  todayData         = signal<TodaySnapshot | null>(null);
  weeklyData        = signal<WeeklySummary | null>(null);
  weightPts         = signal<WeightPoint[]>([]);
  weeklyActivities  = signal<ActivityEntry[]>([]);
  dailyNutrition    = signal<DailyNutritionSummary | null>(null);
  todayFoodLogs     = signal<FoodLog[]>([]);

  addingWeight  = signal(false);
  savingActivity = signal(false);
  activitySaved  = signal(false);
  savingFood     = signal(false);

  newWeight: number | null = null;
  actForm = { date: today(), steps: null as number | null, caloriesBurned: null as number | null, notes: '' };
  foodForm = {
    date: today(),
    mealType: 'breakfast' as FoodLog['mealType'],
    description: '',
    calories: null as number | null,
    proteinG: null as number | null,
    carbsG: null as number | null,
    fatG: null as number | null,
  };
  mealTypeOptions = [
    { value: 'breakfast' as const, label: 'Desayuno' },
    { value: 'lunch' as const, label: 'Comida' },
    { value: 'dinner' as const, label: 'Cena' },
    { value: 'snack' as const, label: 'Snack' },
    { value: 'other' as const, label: 'Otro' },
  ];

  today   = this.todayData;
  weekly  = this.weeklyData;

  stepsPct() {
    return Math.min(((this.todayData()?.steps ?? 0) / 8000) * 100, 100);
  }

  calBurnedPct() {
    return Math.min(((this.todayData()?.caloriesBurned ?? 0) / 600) * 100, 100);
  }

  weightData() {
    const pts = this.weightPts();
    return {
      labels: pts.map(p => p.loggedAt.slice(0, 10)),
      datasets: [{
        label: 'Peso (kg)',
        data: pts.map(p => p.weightKg),
        borderColor: '#d4b200',
        backgroundColor: 'rgba(212,178,0,0.08)',
        pointBackgroundColor: '#d4b200',
        pointBorderColor: '#000',
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 2,
        fill: true,
        tension: 0.4,
      }],
    } as ChartData<'line'>;
  }

  stepsData() {
    const activities = this.weeklyActivities();
    if (!activities.length) return { labels: [], datasets: [] } as ChartData<'bar'>;
    const dayLabels = ['L','M','X','J','V','S','D'];
    const stepsPerDay = Array(7).fill(0);
    const baseDate = new Date(Date.now() - 6 * 86400000);
    activities.forEach(a => {
      const d = new Date(a.date + 'T00:00:00');
      const idx = Math.round((d.getTime() - baseDate.getTime()) / 86400000);
      if (idx >= 0 && idx < 7) stepsPerDay[idx] = a.steps ?? 0;
    });
    return {
      labels: dayLabels,
      datasets: [{
        label: 'Pasos',
        data: stepsPerDay,
        backgroundColor: 'rgba(212,178,0,0.25)',
        borderColor: 'rgba(212,178,0,0.6)',
        borderWidth: 1,
        borderRadius: 4,
        hoverBackgroundColor: 'rgba(212,178,0,0.45)',
      }],
    } as ChartData<'bar'>;
  }

  lineOpts: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1a1a1a',
        borderColor: '#2e2e2e',
        borderWidth: 1,
        titleColor: '#888',
        bodyColor: '#f0f0f0',
        callbacks: { label: ctx => ` ${ctx.parsed.y ?? ''} kg` },
      },
    },
    scales: {
      x: {
        grid: { color: '#1a1a1a' },
        ticks: { color: '#555', maxTicksLimit: 6, font: { size: 11 } },
      },
      y: {
        grid: { color: '#1e1e1e' },
        ticks: { color: '#555', font: { size: 11 }, callback: v => `${v} kg` },
      },
    },
  };

  barOpts: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1a1a1a',
        borderColor: '#2e2e2e',
        borderWidth: 1,
        titleColor: '#888',
        bodyColor: '#f0f0f0',
        callbacks: { label: ctx => ` ${(ctx.parsed.y ?? 0).toLocaleString()} pasos` },
      },
    },
    scales: {
      x: { grid: { display: false }, ticks: { color: '#555', font: { size: 11 } } },
      y: { grid: { color: '#1e1e1e' }, ticks: { color: '#555', font: { size: 11 } } },
    },
  };

  ngOnInit() {
    this.svc.loadAll().subscribe({
      next: ({ weightProgress, weeklySummary, today, weeklyActivities }) => {
        this.weightPts.set(weightProgress);
        this.weeklyData.set(weeklySummary);
        this.todayData.set(today);
        this.weeklyActivities.set(weeklyActivities);
      },
      error: () => {},
    });
    this.reloadNutrition();
  }

  addWeight() {
    if (!this.newWeight) return;
    this.addingWeight.set(true);
    this.svc.addWeight({ weightKg: this.newWeight }).subscribe({
      next: () => {
        this.newWeight = null;
        this.addingWeight.set(false);
        this.svc.reloadWeightProgress().subscribe(pts => this.weightPts.set(pts));
        this.svc.reloadToday().subscribe(t => this.todayData.set(t));
      },
      error: () => this.addingWeight.set(false),
    });
  }

  logActivity() {
    this.savingActivity.set(true);
    this.svc.logActivity({
      date: this.actForm.date,
      steps: this.actForm.steps ?? undefined,
      caloriesBurned: this.actForm.caloriesBurned ?? undefined,
      notes: this.actForm.notes || undefined,
    }).subscribe({
      next: () => {
        this.savingActivity.set(false);
        this.activitySaved.set(true);
        this.actForm = { date: today(), steps: null, caloriesBurned: null, notes: '' };
        setTimeout(() => this.activitySaved.set(false), 3000);
        this.svc.reloadToday().subscribe(t => this.todayData.set(t));
        this.svc.reloadWeeklySummary().subscribe(w => this.weeklyData.set(w));
        this.svc.reloadWeeklyActivities().subscribe(a => this.weeklyActivities.set(a));
        this.reloadNutrition();
      },
      error: () => this.savingActivity.set(false),
    });
  }

  addFoodLog() {
    if (!this.foodForm.description.trim() || this.foodForm.calories === null) return;
    this.savingFood.set(true);
    this.nutritionSvc.createFoodLog({
      date: this.foodForm.date,
      mealType: this.foodForm.mealType,
      description: this.foodForm.description.trim(),
      calories: this.foodForm.calories,
      proteinG: this.foodForm.proteinG ?? undefined,
      carbsG: this.foodForm.carbsG ?? undefined,
      fatG: this.foodForm.fatG ?? undefined,
      source: 'manual',
    }).subscribe({
      next: () => {
        this.foodForm = {
          date: today(),
          mealType: 'breakfast',
          description: '',
          calories: null,
          proteinG: null,
          carbsG: null,
          fatG: null,
        };
        this.savingFood.set(false);
        this.reloadNutrition();
      },
      error: () => this.savingFood.set(false),
    });
  }

  private reloadNutrition() {
    this.nutritionSvc.getTodaySummary().subscribe({
      next: summary => this.dailyNutrition.set(summary),
      error: () => this.dailyNutrition.set(null),
    });
    this.nutritionSvc.getTodayFoodLogs().subscribe({
      next: foods => this.todayFoodLogs.set(foods),
      error: () => this.todayFoodLogs.set([]),
    });
  }
}
