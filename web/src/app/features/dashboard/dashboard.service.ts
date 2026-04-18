import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { forkJoin } from 'rxjs';

export interface WeightPoint   { date: string; weightKg: number; }
export interface WeeklySummary { stepsTotal: number; caloriesBurnedTotal: number; daysLogged: number; avgSteps: number; }
export interface TodaySnapshot { stepsToday: number; caloriesBurnedToday: number; weightDelta7d: number | null; }

export interface AddWeightReq    { weightKg: number; loggedAt?: string; }
export interface AddActivityReq  { date: string; steps?: number; caloriesBurned?: number; notes?: string; }

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  loadAll(days = 90) {
    return forkJoin({
      weightProgress: this.http.get<WeightPoint[]>(`${this.base}/dashboard/weight-progress?days=${days}`),
      weeklySummary:  this.http.get<WeeklySummary>(`${this.base}/dashboard/weekly-summary`),
      today:          this.http.get<TodaySnapshot>(`${this.base}/dashboard/today`),
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
}
