package com.fitnesscoach.profile;

import java.time.Instant;

public record ProfileResponse(
    String displayName,
    Integer age,
    Sex sex,
    Double heightCm,
    Double currentWeightKg,
    ActivityLevel activityLevel,
    Integer weeklyExerciseDays,
    String exerciseType,
    Integer exerciseMinutes,
    Integer dailySteps,
    DietType dietType,
    Goal goal,
    Instant updatedAt) {

  static ProfileResponse from(MetabolicProfile p, String displayName) {
    return new ProfileResponse(
        displayName,
        p.getAge(),
        p.getSex(),
        p.getHeightCm(),
        p.getCurrentWeightKg(),
        p.getActivityLevel(),
        p.getWeeklyExerciseDays(),
        p.getExerciseType(),
        p.getExerciseMinutes(),
        p.getDailySteps(),
        p.getDietType(),
        p.getGoal(),
        p.getUpdatedAt());
  }
}
