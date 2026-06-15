package com.fitnesscoach.activity;

import java.time.Instant;
import java.time.LocalDate;

public record ActivityLogResponse(
    Long id,
    LocalDate date,
    Integer steps,
    Integer caloriesBurned,
    String notes,
    String source,
    Instant syncedAt) {

  static ActivityLogResponse from(ActivityLog a) {
    return new ActivityLogResponse(
        a.getId(),
        a.getDate(),
        a.getSteps(),
        a.getCaloriesBurned(),
        a.getNotes(),
        a.getSource(),
        a.getSyncedAt());
  }
}
