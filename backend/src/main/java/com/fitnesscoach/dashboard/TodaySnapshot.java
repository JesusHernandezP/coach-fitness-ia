package com.fitnesscoach.dashboard;

public record TodaySnapshot(
    int steps, int caloriesBurned, Double currentWeightKg, Double targetCalories) {}
