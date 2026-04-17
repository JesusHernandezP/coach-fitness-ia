package com.fitnesscoach.profile;

import java.time.Instant;

public record NutritionTargetResponse(
    Double calories, Double proteinG, Double carbsG, Double fatG, Instant calculatedAt) {

  static NutritionTargetResponse from(NutritionTarget t) {
    return new NutritionTargetResponse(
        t.getCalories(), t.getProteinG(), t.getCarbsG(), t.getFatG(), t.getCalculatedAt());
  }
}
