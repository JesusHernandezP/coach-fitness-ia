import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface MetabolicProfile {
  age: number;
  sex: 'MALE' | 'FEMALE';
  heightCm: number;
  currentWeightKg: number;
  activityLevel: 'SEDENTARY' | 'LIGHTLY_ACTIVE' | 'MODERATELY_ACTIVE' | 'VERY_ACTIVE' | 'EXTRA_ACTIVE';
  goal: 'LOSE' | 'MAINTAIN' | 'GAIN';
  dietType: 'STANDARD' | 'KETO' | 'VEGETARIAN' | 'INTERMITTENT_FASTING';
  weeklyExerciseDays?: number;
  exerciseMinutes?: number;
  dailySteps?: number;
}

export interface NutritionTarget {
  calories: number;
  proteinG: number;
  carbsG: number;
  fatG: number;
  calculatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/profile`;

  getProfile() {
    return this.http.get<MetabolicProfile>(`${this.base}/me`);
  }

  saveProfile(profile: MetabolicProfile) {
    return this.http.put<MetabolicProfile>(`${this.base}/me`, profile);
  }

  getTargets() {
    return this.http.get<NutritionTarget>(`${this.base}/targets`);
  }
}
