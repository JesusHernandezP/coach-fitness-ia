package com.fitnessaicoach.app.data.repository

import com.fitnessaicoach.app.data.network.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(private val api: ApiService) {

    suspend fun weightProgress(days: Int = 90): Result<List<WeightPoint>> =
        runCatching { api.weightProgress(days) }

    suspend fun weeklySummary(): Result<WeeklySummary> =
        runCatching { api.weeklySummary() }

    suspend fun today(): Result<TodaySnapshot> =
        runCatching { api.dashboardToday() }

    suspend fun todayNutrition(): Result<DailyNutritionSummaryDto> =
        runCatching { api.nutritionToday() }

    suspend fun todayFoodLogs(): Result<List<FoodLogDto>> =
        runCatching { api.todayFoodLogs() }

    suspend fun createFoodLog(
        date: String,
        mealType: String,
        description: String,
        calories: Double,
        proteinG: Double? = null,
        carbsG: Double? = null,
        fatG: Double? = null,
    ): Result<FoodLogDto> = runCatching {
        api.createFoodLog(FoodLogRequest(date, mealType, description, calories, proteinG, carbsG, fatG))
    }

    suspend fun addWeight(weightKg: Double, loggedAt: String? = null): Result<WeightLog> =
        runCatching { api.addWeight(WeightLogRequest(weightKg, loggedAt)) }

    suspend fun logActivity(
        date: String,
        steps: Int? = null,
        caloriesBurned: Int? = null,
        notes: String? = null,
    ): Result<Unit> = runCatching {
        api.logActivity(ActivityLogRequest(date, steps, caloriesBurned, notes))
    }
}
