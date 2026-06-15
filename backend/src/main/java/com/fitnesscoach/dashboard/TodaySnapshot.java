package com.fitnesscoach.dashboard;

public record TodaySnapshot(
    Double targetCalories,
    double consumedCalories,
    Double remainingCalories,
    Double targetProteinG,
    double consumedProteinG,
    Double remainingProteinG,
    int steps,
    int caloriesBurned,
    Double currentWeightKg) {}
