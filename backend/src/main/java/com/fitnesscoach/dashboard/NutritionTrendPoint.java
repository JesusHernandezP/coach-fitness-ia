package com.fitnesscoach.dashboard;

import java.time.LocalDate;

public record NutritionTrendPoint(
    LocalDate date,
    double consumedCalories,
    Double targetCalories,
    double consumedProteinG,
    Double targetProteinG) {}
