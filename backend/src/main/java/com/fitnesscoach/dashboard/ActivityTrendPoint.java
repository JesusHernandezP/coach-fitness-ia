package com.fitnesscoach.dashboard;

import java.time.LocalDate;

public record ActivityTrendPoint(LocalDate date, int steps, int caloriesBurned) {}
