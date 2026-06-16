package com.fitnesscoach.dashboard;

public record WeeklyKpisSnapshot(
    int daysWithMealsLogged,
    double avgCaloriesConsumed,
    double avgProteinConsumed,
    double avgSteps,
    long activeCaloriesTotal,
    Double weightDelta,
    Double caloricAdherencePct,
    int loggingStreakDays) {}
