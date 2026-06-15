package com.fitnesscoach.nutrition;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record DailyNutritionSummary(
    @Schema(example = "2026-06-15") LocalDate date,
    @Schema(example = "2360") Double targetCalories,
    @Schema(example = "1280") Double consumedCalories,
    @Schema(example = "1080") Double remainingCalories,
    @Schema(example = "160") Double targetProteinG,
    @Schema(example = "92") Double consumedProteinG,
    @Schema(example = "68") Double remainingProteinG,
    @Schema(example = "286") Double targetCarbsG,
    @Schema(example = "140") Double consumedCarbsG,
    @Schema(example = "146") Double remainingCarbsG,
    @Schema(example = "64") Double targetFatG,
    @Schema(example = "41") Double consumedFatG,
    @Schema(example = "23") Double remainingFatG,
    @Schema(example = "430") Integer activityCaloriesBurned,
    @Schema(example = "850") Double netCalories) {}
