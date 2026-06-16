import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { forkJoin } from 'rxjs';

export interface WeightPoint   { loggedAt: string; weightKg: number; }
export interface WeeklySummary { stepsTotal: number; caloriesBurnedTotal: number; daysLogged: number; avgSteps: number; weightDelta: number | null; }
export interface WeeklyKpis {
  daysWithMealsLogged: number;
  avgCaloriesConsumed: number;
  avgProteinConsumed: number;
  avgSteps: number;
  activeCaloriesTotal: number;
  weightDelta: number | null;
  caloricAdherencePct: number | null;
  loggingStreakDays: number;
}
export interface TodaySnapshot {
  targetCalories: number | null;
  consumedCalories: number;
  remainingCalories: number | null;
  targetProteinG: number | null;
  consumedProteinG: number;
  remainingProteinG: number | null;
  steps: number;
  caloriesBurned: number;
  currentWeightKg: number | null;
  activitySource: string | null;
  activitySyncedAt: string | null;
}
export interface ActivityEntry { date: string; steps: number | null; caloriesBurned: number | null; }
export interface NutritionTrendPoint {
  date: string;
  consumedCalories: number;
  targetCalories: number | null;
  consumedProteinG: number;
  targetProteinG: number | null;
}
export interface ActivityTrendPoint {
  date: string;
  steps: number;
  caloriesBurned: number;
}
export interface AdherencePoint {
  date: string;
  consumedCalories: number;
  targetCalories: number | null;
  adherencePct: number | null;
}
export interface WeeklyReview {
  periodStart: string;
  periodEnd: string;
  summary: string;
  nutritionFindings: string[];
  activityFindings: string[];
  weightFindings: string[];
  recommendations: string[];
  riskNotes: string[];
}

export interface AddWeightReq    { weightKg: number; loggedAt?: string; }
export interface AddActivityReq  { date: string; steps?: number; caloriesBurned?: number; notes?: string; }

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  loadAll(days = 90) {
    const weekAgo = new Date(Date.now() - 6 * 86400000).toISOString().slice(0, 10);
    const todayStr = new Date().toISOString().slice(0, 10);
    return forkJoin({
      weightProgress:    this.http.get<WeightPoint[]>(`${this.base}/dashboard/weight-progress?days=${days}`),
      weeklySummary:     this.http.get<WeeklySummary>(`${this.base}/dashboard/weekly-summary`),
      weeklyKpis:        this.http.get<WeeklyKpis>(`${this.base}/dashboard/weekly-kpis`),
      today:             this.http.get<TodaySnapshot>(`${this.base}/dashboard/today`),
      nutritionTrend:    this.http.get<NutritionTrendPoint[]>(`${this.base}/dashboard/nutrition-trend?days=30`),
      activityTrend:     this.http.get<ActivityTrendPoint[]>(`${this.base}/dashboard/activity-trend?days=30`),
      adherenceTrend:    this.http.get<AdherencePoint[]>(`${this.base}/dashboard/adherence?days=30`),
      weeklyActivities:  this.http.get<ActivityEntry[]>(`${this.base}/activities?from=${weekAgo}&to=${todayStr}`),
    });
  }

  addWeight(req: AddWeightReq) {
    return this.http.post<void>(`${this.base}/weights`, req);
  }

  logActivity(req: AddActivityReq) {
    return this.http.post<void>(`${this.base}/activities`, req);
  }

  reloadToday() {
    return this.http.get<TodaySnapshot>(`${this.base}/dashboard/today`);
  }

  reloadWeightProgress(days = 90) {
    return this.http.get<WeightPoint[]>(`${this.base}/dashboard/weight-progress?days=${days}`);
  }

  reloadWeeklySummary() {
    return this.http.get<WeeklySummary>(`${this.base}/dashboard/weekly-summary`);
  }

  reloadWeeklyKpis() {
    return this.http.get<WeeklyKpis>(`${this.base}/dashboard/weekly-kpis`);
  }

  reloadNutritionTrend(days = 30) {
    return this.http.get<NutritionTrendPoint[]>(`${this.base}/dashboard/nutrition-trend?days=${days}`);
  }

  reloadActivityTrend(days = 30) {
    return this.http.get<ActivityTrendPoint[]>(`${this.base}/dashboard/activity-trend?days=${days}`);
  }

  reloadAdherenceTrend(days = 30) {
    return this.http.get<AdherencePoint[]>(`${this.base}/dashboard/adherence?days=${days}`);
  }

  generateWeeklyReview() {
    return this.http.get<WeeklyReview>(`${this.base}/coach/weekly-review`);
  }

  reloadWeeklyActivities() {
    const weekAgo = new Date(Date.now() - 6 * 86400000).toISOString().slice(0, 10);
    const todayStr = new Date().toISOString().slice(0, 10);
    return this.http.get<ActivityEntry[]>(`${this.base}/activities?from=${weekAgo}&to=${todayStr}`);
  }
}
