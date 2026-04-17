package com.fitnesscoach.dashboard;

public record WeeklySummary(
    int daysLogged,
    long stepsTotal,
    long caloriesBurnedTotal,
    double avgSteps,
    Double weightDelta) {}
