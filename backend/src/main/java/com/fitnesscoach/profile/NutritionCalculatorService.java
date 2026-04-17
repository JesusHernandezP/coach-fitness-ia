package com.fitnesscoach.profile;

import org.springframework.stereotype.Service;

@Service
public class NutritionCalculatorService {

  private static final double MIN_PROTEIN_PER_KG = 1.8;

  public NutritionResult calculate(MetabolicProfile p) {
    double bmr = computeBmr(p);
    double tdee = bmr * activityFactor(p.getActivityLevel());
    double calories = tdee + goalAdjustment(p.getGoal());

    MacroRatio ratio = macroRatio(p.getDietType());
    double proteinCals = calories * ratio.proteinPct();
    double carbsCals = calories * ratio.carbsPct();
    double fatCals = calories * ratio.fatPct();

    double proteinG = Math.max(proteinCals / 4.0, MIN_PROTEIN_PER_KG * p.getCurrentWeightKg());
    double carbsG = carbsCals / 4.0;
    double fatG = fatCals / 9.0;

    return new NutritionResult(
        Math.round(calories * 100.0) / 100.0,
        Math.round(proteinG * 100.0) / 100.0,
        Math.round(carbsG * 100.0) / 100.0,
        Math.round(fatG * 100.0) / 100.0);
  }

  private double computeBmr(MetabolicProfile p) {
    double base = 10 * p.getCurrentWeightKg() + 6.25 * p.getHeightCm() - 5 * p.getAge();
    return p.getSex() == Sex.MALE ? base + 5 : base - 161;
  }

  private double activityFactor(ActivityLevel level) {
    return switch (level) {
      case SEDENTARY -> 1.2;
      case LIGHTLY_ACTIVE -> 1.375;
      case MODERATELY_ACTIVE -> 1.55;
      case VERY_ACTIVE -> 1.725;
      case EXTRA_ACTIVE -> 1.9;
    };
  }

  private double goalAdjustment(Goal goal) {
    return switch (goal) {
      case LOSE_WEIGHT -> -500;
      case MAINTAIN -> 0;
      case GAIN_WEIGHT -> 300;
    };
  }

  private MacroRatio macroRatio(DietType diet) {
    return switch (diet) {
      case STANDARD -> new MacroRatio(0.30, 0.40, 0.30);
      case KETO -> new MacroRatio(0.30, 0.10, 0.60);
      case VEGETARIAN -> new MacroRatio(0.25, 0.50, 0.25);
      case INTERMITTENT_FASTING -> new MacroRatio(0.35, 0.35, 0.30);
    };
  }

  public record NutritionResult(double calories, double proteinG, double carbsG, double fatG) {}

  private record MacroRatio(double proteinPct, double carbsPct, double fatPct) {}
}
