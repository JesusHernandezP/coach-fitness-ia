package com.fitnesscoach.nutrition;

import java.time.Instant;
import java.time.LocalDate;

public record FoodLogResponse(
    Long id,
    LocalDate date,
    MealType mealType,
    String description,
    Double calories,
    Double proteinG,
    Double carbsG,
    Double fatG,
    FoodLogSource source,
    Double confidence,
    Instant createdAt,
    Instant updatedAt) {

  static FoodLogResponse from(FoodLog log) {
    return new FoodLogResponse(
        log.getId(),
        log.getDate(),
        log.getMealType(),
        log.getDescription(),
        log.getCalories(),
        log.getProteinG(),
        log.getCarbsG(),
        log.getFatG(),
        log.getSource(),
        log.getConfidence(),
        log.getCreatedAt(),
        log.getUpdatedAt());
  }
}
