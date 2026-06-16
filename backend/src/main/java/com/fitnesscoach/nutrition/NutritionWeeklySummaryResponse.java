package com.fitnesscoach.nutrition;

import java.util.List;

public record NutritionWeeklySummaryResponse(List<DailyNutritionSummary> days) {}
