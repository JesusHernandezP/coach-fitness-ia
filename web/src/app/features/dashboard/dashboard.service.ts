import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { forkJoin } from 'rxjs';

export interface WeightPoint   { loggedAt: string; weightKg: number; }
export interface WeeklySummary { stepsTotal: number; caloriesBurnedTotal: number; daysLogged: number; avgSteps: number; weightDelta: number | null; }
export interface TodaySnapshot { steps: number; caloriesBurned: number; currentWeightKg: number | null; targetCalories: number | null; }
export interface ActivityEntry { date: string; steps: number | null; caloriesBurned: number | null; }

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
      today:             this.http.get<TodaySnapshot>(`${this.base}/dashboard/today`),
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

  reloadWeeklyActivities() {
    const weekAgo = new Date(Date.now() - 6 * 86400000).toISOString().slice(0, 10);
    const todayStr = new Date().toISOString().slice(0, 10);
    return this.http.get<ActivityEntry[]>(`${this.base}/activities?from=${weekAgo}&to=${todayStr}`);
  }
}
