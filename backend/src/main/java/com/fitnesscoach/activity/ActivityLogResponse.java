package com.fitnesscoach.activity;

import java.time.LocalDate;

public record ActivityLogResponse(
    Long id, LocalDate date, Integer steps, Integer caloriesBurned, String notes) {

  static ActivityLogResponse from(ActivityLog a) {
    return new ActivityLogResponse(
        a.getId(), a.getDate(), a.getSteps(), a.getCaloriesBurned(), a.getNotes());
  }
}
