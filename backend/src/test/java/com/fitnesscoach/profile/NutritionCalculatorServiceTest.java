package com.fitnesscoach.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class NutritionCalculatorServiceTest {

  private final NutritionCalculatorService calculator = new NutritionCalculatorService();

  // Reference case from plan: male, 30y, 170cm, 80kg, moderately active, lose weight, standard
  // Expected: ~2360 kcal, ~160g protein, ~286g carbs, ~64g fat
  @Test
  void calculatesTdeeCorrectly_referenceCase() {
    MetabolicProfile profile =
        MetabolicProfile.builder()
            .age(30)
            .sex(Sex.MALE)
            .heightCm(170.0)
            .currentWeightKg(80.0)
            .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
            .goal(Goal.LOSE_WEIGHT)
            .dietType(DietType.STANDARD)
            .build();

    NutritionCalculatorService.NutritionResult result = calculator.calculate(profile);

    // BMR = 10*80 + 6.25*170 - 5*30 + 5 = 1717.5
    // TDEE = 1717.5 * 1.55 = 2662.13 → lose weight -500 = 2162.13
    assertThat(result.calories()).isCloseTo(2162.13, within(1.0));
    // protein floor: 1.8 * 80 = 144g vs 30% split = 162g → split wins
    assertThat(result.proteinG()).isCloseTo(162.16, within(1.0));
    assertThat(result.carbsG()).isCloseTo(216.21, within(1.0));
    assertThat(result.fatG()).isCloseTo(72.07, within(1.0));
  }

  @Test
  void proteinFloor_appliedWhenMacroSplitTooLow() {
    // Very heavy person: macro split protein < 1.8 g/kg floor
    MetabolicProfile profile =
        MetabolicProfile.builder()
            .age(25)
            .sex(Sex.MALE)
            .heightCm(180.0)
            .currentWeightKg(120.0)
            .activityLevel(ActivityLevel.SEDENTARY)
            .goal(Goal.LOSE_WEIGHT)
            .dietType(DietType.KETO)
            .build();

    NutritionCalculatorService.NutritionResult result = calculator.calculate(profile);

    assertThat(result.proteinG()).isGreaterThanOrEqualTo(1.8 * 120.0);
  }

  @Test
  void goalAdjustment_gainWeight_addsCalories() {
    MetabolicProfile lose =
        MetabolicProfile.builder()
            .age(25)
            .sex(Sex.FEMALE)
            .heightCm(165.0)
            .currentWeightKg(60.0)
            .activityLevel(ActivityLevel.LIGHTLY_ACTIVE)
            .goal(Goal.LOSE_WEIGHT)
            .dietType(DietType.STANDARD)
            .build();

    MetabolicProfile gain =
        MetabolicProfile.builder()
            .age(25)
            .sex(Sex.FEMALE)
            .heightCm(165.0)
            .currentWeightKg(60.0)
            .activityLevel(ActivityLevel.LIGHTLY_ACTIVE)
            .goal(Goal.GAIN_WEIGHT)
            .dietType(DietType.STANDARD)
            .build();

    double diff = calculator.calculate(gain).calories() - calculator.calculate(lose).calories();
    assertThat(diff).isCloseTo(800.0, within(1.0));
  }
}
