import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import {
  ActivityTrendPoint,
  AdherencePoint,
  DashboardService,
  NutritionTrendPoint,
  TodaySnapshot,
  WeeklyReview,
  WeeklyKpis,
  WeightPoint,
} from './dashboard.service';
import { DailyNutritionSummary, FoodLog, NutritionService } from './nutrition.service';

const today = () => new Date().toISOString().slice(0, 10);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule, DecimalPipe, DatePipe, BaseChartDirective],
  template: `
    <div class="dash-page">
      <header class="page-header">
        <div>
          <p class="eyebrow">Panel integral</p>
          <h1>Hoy, semana y evolución</h1>
        </div>
        <span class="header-date">{{ now | date:'EEEE, d MMMM yyyy' }}</span>
      </header>

      <section class="section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Hoy</p>
            <h2>KPIs diarios</h2>
          </div>
        </div>
        <div class="kpi-grid kpi-grid-today">
          <article class="kpi-card">
            <span class="kpi-label">Calorías</span>
            <strong>{{ todayData()?.consumedCalories ?? 0 | number:'1.0-0' }}</strong>
            <small>objetivo {{ todayData()?.targetCalories ?? 0 | number:'1.0-0' }} · restan {{ todayData()?.remainingCalories ?? 0 | number:'1.0-0' }}</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Proteína</span>
            <strong>{{ todayData()?.consumedProteinG ?? 0 | number:'1.0-0' }} g</strong>
            <small>objetivo {{ todayData()?.targetProteinG ?? 0 | number:'1.0-0' }} · restan {{ todayData()?.remainingProteinG ?? 0 | number:'1.0-0' }}</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Pasos</span>
            <strong>{{ todayData()?.steps ?? 0 | number }}</strong>
            <small>actividad del día</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Kcal activas</span>
            <strong>{{ todayData()?.caloriesBurned ?? 0 | number }}</strong>
            <small>quemas registradas</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Peso actual</span>
            <strong>{{ todayData()?.currentWeightKg ?? 0 | number:'1.1-1' }} kg</strong>
            <small>último valor del perfil</small>
          </article>
        </div>
      </section>

      <section class="section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Semana</p>
            <h2>Resumen semanal</h2>
          </div>
        </div>
        <div class="kpi-grid">
          <article class="kpi-card">
            <span class="kpi-label">Días con comidas</span>
            <strong>{{ weeklyKpisData()?.daysWithMealsLogged ?? 0 }}/7</strong>
            <small>racha {{ weeklyKpisData()?.loggingStreakDays ?? 0 }} días</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Calorías promedio</span>
            <strong>{{ weeklyKpisData()?.avgCaloriesConsumed ?? 0 | number:'1.0-0' }}</strong>
            <small>por día</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Proteína promedio</span>
            <strong>{{ weeklyKpisData()?.avgProteinConsumed ?? 0 | number:'1.0-0' }} g</strong>
            <small>por día</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Pasos promedio</span>
            <strong>{{ weeklyKpisData()?.avgSteps ?? 0 | number:'1.0-0' }}</strong>
            <small>por día</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Kcal activas</span>
            <strong>{{ weeklyKpisData()?.activeCaloriesTotal ?? 0 | number }}</strong>
            <small>últimos 7 días</small>
          </article>
          <article class="kpi-card">
            <span class="kpi-label">Adherencia</span>
            <strong>{{ weeklyKpisData()?.caloricAdherencePct ?? 0 | number:'1.0-0' }}%</strong>
            <small>calórica semanal</small>
          </article>
        </div>

        <div class="panel-card weekly-review-card">
          <div class="card-head">
            <div>
              <p class="eyebrow">Semana</p>
              <h3>Revisión semanal IA</h3>
            </div>
            <button class="btn" (click)="generateWeeklyReview()" [disabled]="weeklyReviewLoading()">
              {{ weeklyReviewLoading() ? 'Generando...' : 'Generar revisión' }}
            </button>
          </div>

          @if (weeklyReview(); as review) {
            <div class="review-period">{{ review.periodStart }} → {{ review.periodEnd }}</div>
            <p class="review-summary">{{ review.summary }}</p>
            <div class="review-grid">
              <div>
                <span class="review-label">Nutrición</span>
                @for (item of review.nutritionFindings; track item) {
                  <p class="review-item">{{ item }}</p>
                }
              </div>
              <div>
                <span class="review-label">Actividad</span>
                @for (item of review.activityFindings; track item) {
                  <p class="review-item">{{ item }}</p>
                }
              </div>
              <div>
                <span class="review-label">Peso</span>
                @for (item of review.weightFindings; track item) {
                  <p class="review-item">{{ item }}</p>
                }
              </div>
              <div>
                <span class="review-label">Recomendaciones</span>
                @for (item of review.recommendations; track item) {
                  <p class="review-item">{{ item }}</p>
                }
              </div>
            </div>
            @if (review.riskNotes.length) {
              <div class="review-risks">
                @for (item of review.riskNotes; track item) {
                  <p>{{ item }}</p>
                }
              </div>
            }
          } @else {
            <div class="empty compact">Genera una revisión para resumir la semana con IA.</div>
          }
        </div>

      </section>

      <section class="section evolution-grid">
        <div class="panel-card">
          <div class="card-head">
            <div>
              <p class="eyebrow">Evolución</p>
              <h3>Peso</h3>
            </div>
            <div class="inline-form">
              <input class="field" type="number" [(ngModel)]="newWeight" placeholder="kg" step="0.1" min="30" max="300" />
              <button class="btn" (click)="addWeight()" [disabled]="addingWeight() || !newWeight">+ peso</button>
            </div>
          </div>
          @if (weightChart().labels?.length) {
            <div class="chart-wrap"><canvas baseChart [data]="weightChart()" [options]="lineOpts" type="line"></canvas></div>
          } @else {
            <div class="empty">Sin registros de peso.</div>
          }
        </div>

        <div class="panel-card">
          <div class="card-head">
            <div>
              <p class="eyebrow">Evolución</p>
              <h3>Nutrición</h3>
            </div>
          </div>
          @if (nutritionTrendData().length) {
            <div class="chart-wrap"><canvas baseChart [data]="nutritionChart()" [options]="lineOpts" type="line"></canvas></div>
          } @else {
            <div class="empty">Sin datos nutricionales.</div>
          }
        </div>

        <div class="panel-card">
          <div class="card-head">
            <div>
              <p class="eyebrow">Evolución</p>
              <h3>Actividad</h3>
            </div>
          </div>
          @if (activityTrendData().length) {
            <div class="chart-wrap"><canvas baseChart [data]="activityChart()" [options]="barOpts" type="bar"></canvas></div>
          } @else {
            <div class="empty">Sin actividad registrada.</div>
          }
        </div>

        <div class="panel-card">
          <div class="card-head">
            <div>
              <p class="eyebrow">Evolución</p>
              <h3>Adherencia</h3>
            </div>
          </div>
          @if (adherenceTrendData().length) {
            <div class="chart-wrap"><canvas baseChart [data]="adherenceChart()" [options]="lineOpts" type="line"></canvas></div>
          } @else {
            <div class="empty">Sin objetivo calórico.</div>
          }
        </div>
      </section>

      <section class="section journal-grid">
        <div class="panel-card">
          <div class="card-head">
            <div>
              <p class="eyebrow">Diario reciente</p>
              <h3>Registrar actividad</h3>
            </div>
          </div>
          <div class="form-grid">
            <input class="field" type="date" [(ngModel)]="actForm.date" name="actDate" />
            <input class="field" type="number" [(ngModel)]="actForm.steps" name="actSteps" min="0" placeholder="Pasos" />
            <input class="field" type="number" [(ngModel)]="actForm.caloriesBurned" name="actCal" min="0" placeholder="Kcal quemadas" />
            <input class="field field-wide" type="text" [(ngModel)]="actForm.notes" name="actNotes" placeholder="Notas" />
          </div>
          <button class="btn btn-full" (click)="logActivity()" [disabled]="savingActivity()">
            {{ savingActivity() ? 'Guardando...' : 'Guardar actividad' }}
          </button>
        </div>

        <div class="panel-card">
          <div class="card-head">
            <div>
              <p class="eyebrow">Diario reciente</p>
              <h3>Comidas de hoy</h3>
            </div>
          </div>

          @if (dailyNutrition(); as summary) {
            <div class="nutrition-summary">
              <span>{{ summary.consumedCalories | number:'1.0-0' }} kcal</span>
              <small>P {{ summary.consumedProteinG | number:'1.0-0' }} · C {{ summary.consumedCarbsG | number:'1.0-0' }} · G {{ summary.consumedFatG | number:'1.0-0' }}</small>
            </div>
          }

          <div class="form-grid">
            <select class="field" [(ngModel)]="foodForm.mealType" name="mealType">
              @for (option of mealTypeOptions; track option.value) {
                <option [value]="option.value">{{ option.label }}</option>
              }
            </select>
            <input class="field" type="number" [(ngModel)]="foodForm.calories" name="foodCalories" min="0" placeholder="Kcal" />
            <input class="field field-wide" type="text" [(ngModel)]="foodForm.description" name="foodDescription" placeholder="Descripción" />
            <input class="field" type="number" [(ngModel)]="foodForm.proteinG" name="foodProtein" min="0" placeholder="Proteína" />
            <input class="field" type="number" [(ngModel)]="foodForm.carbsG" name="foodCarbs" min="0" placeholder="Carbs" />
            <input class="field" type="number" [(ngModel)]="foodForm.fatG" name="foodFat" min="0" placeholder="Grasa" />
          </div>
          <button class="btn btn-full" (click)="addFoodLog()" [disabled]="savingFood() || !foodForm.description.trim() || foodForm.calories === null">
            {{ savingFood() ? 'Guardando...' : 'Guardar comida' }}
          </button>

          @if (todayFoodLogs().length) {
            <div class="log-list">
              @for (food of todayFoodLogs(); track food.id) {
                <div class="log-row">
                  <div>
                    <strong>{{ food.description }}</strong>
                    <small>{{ food.mealType }} · P {{ food.proteinG ?? 0 | number:'1.0-0' }} · C {{ food.carbsG ?? 0 | number:'1.0-0' }} · G {{ food.fatG ?? 0 | number:'1.0-0' }}</small>
                  </div>
                  <span>{{ food.calories | number:'1.0-0' }} kcal</span>
                </div>
              }
            </div>
          } @else {
            <div class="empty compact">Aún no hay comidas registradas hoy.</div>
          }
        </div>
      </section>
    </div>
  `,
  styles: [`
    :host { display: block; }
    .dash-page { max-width: 1240px; margin: 0 auto; display: grid; gap: 1.25rem; }
    .page-header, .section-heading, .card-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; }
    .page-header h1, .section-heading h2, .card-head h3 { margin: 0; color: #f5f5f0; font-family: 'Bebas Neue', sans-serif; letter-spacing: 0.04em; }
    .page-header h1 { font-size: 2.7rem; }
    .section-heading h2, .card-head h3 { font-size: 1.6rem; }
    .eyebrow { margin: 0 0 0.2rem; color: #d4b200; text-transform: uppercase; letter-spacing: 0.14em; font-size: 0.72rem; }
    .header-date { color: #767676; text-transform: capitalize; font-size: 0.8rem; }
    .section { display: grid; gap: 0.9rem; }
    .kpi-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 0.9rem; }
    .kpi-grid-today { grid-template-columns: repeat(5, minmax(0, 1fr)); }
    .kpi-card, .panel-card {
      background: linear-gradient(180deg, rgba(26,26,26,0.98), rgba(15,15,15,0.98));
      border: 1px solid rgba(212,178,0,0.15);
      border-radius: 20px;
      padding: 1rem;
      box-shadow: inset 0 1px 0 rgba(255,255,255,0.02);
    }
    .kpi-card strong { display: block; color: #f5f2dc; font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.03em; }
    .kpi-card small, .log-row small, .nutrition-summary small { color: #8a8a8a; display: block; }
    .kpi-label { display: block; color: #7a7a7a; text-transform: uppercase; font-size: 0.72rem; letter-spacing: 0.12em; margin-bottom: 0.45rem; }
    .evolution-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; }
    .journal-grid { display: grid; grid-template-columns: 0.9fr 1.1fr; gap: 1rem; }
    .weekly-review-card { margin-top: 0.25rem; }
    .chart-wrap { height: 260px; }
    .empty { min-height: 220px; display: grid; place-items: center; color: #6f6f6f; background: rgba(255,255,255,0.02); border-radius: 14px; }
    .empty.compact { min-height: 100px; }
    .inline-form { display: flex; gap: 0.6rem; align-items: center; }
    .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.75rem; margin-bottom: 0.75rem; }
    .field, select.field {
      width: 100%;
      background: #121212;
      color: #efefef;
      border: 1px solid #282828;
      border-radius: 12px;
      padding: 0.75rem 0.9rem;
    }
    .field-wide { grid-column: 1 / -1; }
    .btn {
      border: 0;
      border-radius: 999px;
      padding: 0.75rem 1rem;
      background: #d4b200;
      color: #111;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      cursor: pointer;
    }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-full { width: 100%; }
    .nutrition-summary {
      display: flex; justify-content: space-between; align-items: center; gap: 1rem;
      background: rgba(212,178,0,0.08); border: 1px solid rgba(212,178,0,0.16);
      border-radius: 14px; padding: 0.8rem 0.9rem; margin-bottom: 0.9rem; color: #f5f2dc;
    }
    .log-list { display: grid; gap: 0.7rem; margin-top: 1rem; }
    .log-row {
      display: flex; justify-content: space-between; gap: 1rem; align-items: center;
      background: #121212; border: 1px solid #232323; border-radius: 14px; padding: 0.85rem 0.9rem; color: #efefef;
    }
    .log-row span { color: #d4b200; white-space: nowrap; }
    .review-period { color: #8a8a8a; font-size: 0.8rem; margin-bottom: 0.65rem; }
    .review-summary { color: #f1f1ec; line-height: 1.6; margin-bottom: 1rem; }
    .review-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.9rem; }
    .review-label {
      display: block; color: #d4b200; font-size: 0.72rem; text-transform: uppercase;
      letter-spacing: 0.12em; margin-bottom: 0.35rem;
    }
    .review-item { color: #d8d8d8; font-size: 0.88rem; line-height: 1.5; margin: 0 0 0.35rem; }
    .review-risks {
      margin-top: 0.8rem; padding: 0.9rem; border-radius: 14px;
      background: rgba(249,115,22,0.08); border: 1px solid rgba(249,115,22,0.18); color: #f2b37b;
    }
    .review-risks p { margin: 0 0 0.35rem; }
    .review-risks p:last-child { margin-bottom: 0; }
    @media (max-width: 1100px) {
      .kpi-grid-today, .kpi-grid, .evolution-grid, .journal-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    }
    @media (max-width: 760px) {
      .page-header, .section-heading, .card-head, .nutrition-summary { flex-direction: column; align-items: flex-start; }
      .kpi-grid-today, .kpi-grid, .evolution-grid, .journal-grid, .form-grid, .review-grid { grid-template-columns: 1fr; }
      .chart-wrap { height: 220px; }
    }
  `],
})
export class DashboardComponent implements OnInit {
  private svc = inject(DashboardService);
  private nutritionSvc = inject(NutritionService);

  now = new Date();

  todayData = signal<TodaySnapshot | null>(null);
  weeklyKpisData = signal<WeeklyKpis | null>(null);
  weightPts = signal<WeightPoint[]>([]);
  nutritionTrendData = signal<NutritionTrendPoint[]>([]);
  activityTrendData = signal<ActivityTrendPoint[]>([]);
  adherenceTrendData = signal<AdherencePoint[]>([]);
  dailyNutrition = signal<DailyNutritionSummary | null>(null);
  todayFoodLogs = signal<FoodLog[]>([]);
  weeklyReview = signal<WeeklyReview | null>(null);

  addingWeight = signal(false);
  savingActivity = signal(false);
  savingFood = signal(false);
  weeklyReviewLoading = signal(false);

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

  lineOpts: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#d0d0d0' } } },
    scales: {
      x: { ticks: { color: '#747474' }, grid: { color: '#1e1e1e' } },
      y: { ticks: { color: '#747474' }, grid: { color: '#1e1e1e' } },
    },
  };

  barOpts: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#d0d0d0' } } },
    scales: {
      x: { ticks: { color: '#747474' }, grid: { display: false } },
      y: { ticks: { color: '#747474' }, grid: { color: '#1e1e1e' } },
    },
  };

  ngOnInit() {
    this.reloadDashboard();
    this.reloadNutrition();
  }

  weightChart(): ChartData<'line'> {
    const pts = this.weightPts();
    return {
      labels: pts.map(point => point.loggedAt.slice(0, 10)),
      datasets: [{
        label: 'Peso (kg)',
        data: pts.map(point => point.weightKg),
        borderColor: '#d4b200',
        backgroundColor: 'rgba(212,178,0,0.14)',
        tension: 0.35,
        fill: true,
      }],
    };
  }

  nutritionChart(): ChartData<'line'> {
    const points = this.nutritionTrendData();
    return {
      labels: points.map(point => point.date.slice(5)),
      datasets: [
        {
          label: 'Calorías consumidas',
          data: points.map(point => point.consumedCalories),
          borderColor: '#d4b200',
          backgroundColor: 'rgba(212,178,0,0.08)',
          tension: 0.35,
        },
        {
          label: 'Proteína consumida',
          data: points.map(point => point.consumedProteinG),
          borderColor: '#4ade80',
          backgroundColor: 'rgba(74,222,128,0.08)',
          tension: 0.35,
        },
      ],
    };
  }

  activityChart(): ChartData<'bar'> {
    const points = this.activityTrendData();
    return {
      labels: points.map(point => point.date.slice(5)),
      datasets: [
        {
          label: 'Pasos',
          data: points.map(point => point.steps),
          backgroundColor: 'rgba(212,178,0,0.45)',
        },
        {
          label: 'Kcal activas',
          data: points.map(point => point.caloriesBurned),
          backgroundColor: 'rgba(86,160,255,0.4)',
        },
      ],
    };
  }

  adherenceChart(): ChartData<'line'> {
    const points = this.adherenceTrendData();
    return {
      labels: points.map(point => point.date.slice(5)),
      datasets: [{
        label: 'Adherencia (%)',
        data: points.map(point => point.adherencePct ?? 0),
        borderColor: '#f97316',
        backgroundColor: 'rgba(249,115,22,0.1)',
        tension: 0.35,
        fill: true,
      }],
    };
  }

  addWeight() {
    if (!this.newWeight) return;
    this.addingWeight.set(true);
    this.svc.addWeight({ weightKg: this.newWeight }).subscribe({
      next: () => {
        this.newWeight = null;
        this.addingWeight.set(false);
        this.reloadDashboard();
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
        this.actForm = { date: today(), steps: null, caloriesBurned: null, notes: '' };
        this.savingActivity.set(false);
        this.reloadDashboard();
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
        this.reloadDashboard();
        this.reloadNutrition();
      },
      error: () => this.savingFood.set(false),
    });
  }

  generateWeeklyReview() {
    this.weeklyReviewLoading.set(true);
    this.svc.generateWeeklyReview().subscribe({
      next: review => {
        this.weeklyReview.set(review);
        this.weeklyReviewLoading.set(false);
      },
      error: () => this.weeklyReviewLoading.set(false),
    });
  }

  private reloadDashboard() {
    this.svc.loadAll().subscribe({
      next: data => {
        this.todayData.set(data.today);
        this.weeklyKpisData.set(data.weeklyKpis);
        this.weightPts.set(data.weightProgress);
        this.nutritionTrendData.set(data.nutritionTrend);
        this.activityTrendData.set(data.activityTrend);
        this.adherenceTrendData.set(data.adherenceTrend);
      },
      error: () => {},
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
