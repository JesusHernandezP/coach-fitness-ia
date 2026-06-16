import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface FoodLog {
  id: number;
  date: string;
  mealType: 'breakfast' | 'lunch' | 'dinner' | 'snack' | 'other';
  description: string;
  calories: number;
  proteinG: number | null;
  carbsG: number | null;
  fatG: number | null;
  source: 'manual' | 'ai_estimate';
  confidence: number | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface DailyNutritionSummary {
  date: string;
  targetCalories: number | null;
  consumedCalories: number;
  remainingCalories: number | null;
  targetProteinG: number | null;
  consumedProteinG: number;
  remainingProteinG: number | null;
  targetCarbsG: number | null;
  consumedCarbsG: number;
  remainingCarbsG: number | null;
  targetFatG: number | null;
  consumedFatG: number;
  remainingFatG: number | null;
  activityCaloriesBurned: number;
  netCalories: number;
}

export interface CreateFoodLogRequest {
  date: string;
  mealType: FoodLog['mealType'];
  description: string;
  calories: number;
  proteinG?: number;
  carbsG?: number;
  fatG?: number;
  source: 'manual';
}

@Injectable({ providedIn: 'root' })
export class NutritionService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  getTodaySummary() {
    return this.http.get<DailyNutritionSummary>(`${this.base}/nutrition/today`);
  }

  getTodayFoodLogs() {
    return this.http.get<FoodLog[]>(`${this.base}/food-logs/today`);
  }

  createFoodLog(request: CreateFoodLogRequest) {
    return this.http.post<FoodLog>(`${this.base}/food-logs`, request);
  }
}
