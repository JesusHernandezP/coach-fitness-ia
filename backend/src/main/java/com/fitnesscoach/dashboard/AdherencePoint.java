package com.fitnesscoach.dashboard;

import java.time.LocalDate;

public record AdherencePoint(
    LocalDate date, double consumedCalories, Double targetCalories, Double adherencePct) {}
