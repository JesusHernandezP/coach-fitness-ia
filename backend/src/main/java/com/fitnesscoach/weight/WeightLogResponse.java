package com.fitnesscoach.weight;

import java.time.Instant;

public record WeightLogResponse(Long id, Double weightKg, Instant loggedAt) {

  static WeightLogResponse from(WeightLog w) {
    return new WeightLogResponse(w.getId(), w.getWeightKg(), w.getLoggedAt());
  }
}
