package com.fitnesscoach.chat;

import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.nutrition.DailyNutritionSummary;
import com.fitnesscoach.nutrition.FoodLogRepository;
import com.fitnesscoach.nutrition.FoodLogService;
import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.weight.WeightLogRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiContextService {

  private final MetabolicProfileRepository profileRepository;
  private final NutritionTargetRepository targetRepository;
  private final FoodLogRepository foodLogRepository;
  private final FoodLogService foodLogService;
  private final ActivityLogRepository activityLogRepository;
  private final WeightLogRepository weightLogRepository;
  private final RagService ragService;

  public AiContextSnapshot build(Long userId, String userText) {
    StringBuilder prompt = new StringBuilder();
    var profileOpt = profileRepository.findByUserId(userId);
    if (profileOpt.isEmpty()) {
      return new AiContextSnapshot(
          "El usuario aun no ha completado su perfil metabolico. Si pide calculos personalizados o registro nutricional, indicale que complete su perfil.",
          false);
    }

    var profile = profileOpt.get();
    prompt
        .append("Perfil: ")
        .append(profile.getSex())
        .append(", ")
        .append(profile.getAge())
        .append(" anos, ")
        .append(profile.getHeightCm())
        .append(" cm, ")
        .append(profile.getCurrentWeightKg())
        .append(" kg, objetivo ")
        .append(profile.getGoal())
        .append(", dieta ")
        .append(profile.getDietType())
        .append(". ");

    targetRepository
        .findByUserId(userId)
        .ifPresent(
            target ->
                prompt.append(
                    String.format(
                        "Objetivos diarios: %.0f kcal, %.0f g proteina, %.0f g carbs, %.0f g grasa. ",
                        target.getCalories(),
                        target.getProteinG(),
                        target.getCarbsG(),
                        target.getFatG())));

    DailyNutritionSummary today = foodLogService.todaySummary(userId);
    prompt.append(
        String.format(
            "Consumido hoy: %.0f kcal, %.0f g proteina, %.0f g carbs, %.0f g grasa. Netas hoy: %.0f. ",
            today.consumedCalories(),
            today.consumedProteinG(),
            today.consumedCarbsG(),
            today.consumedFatG(),
            today.netCalories()));

    var todayFoods =
        foodLogRepository.findByUserIdAndDateOrderByCreatedAtAsc(userId, LocalDate.now());
    if (!todayFoods.isEmpty()) {
      prompt.append("Comidas de hoy: ");
      todayFoods.stream()
          .limit(5)
          .forEach(
              log ->
                  prompt
                      .append(log.getMealType())
                      .append(": ")
                      .append(log.getDescription())
                      .append(" (")
                      .append(log.getCalories().intValue())
                      .append(" kcal). "));
    }

    activityLogRepository
        .findByUserIdAndDate(userId, LocalDate.now())
        .ifPresent(
            activity ->
                prompt.append(
                    String.format(
                        "Actividad hoy: %d pasos y %d kcal quemadas. ",
                        activity.getSteps() != null ? activity.getSteps() : 0,
                        activity.getCaloriesBurned() != null ? activity.getCaloriesBurned() : 0)));

    weightLogRepository
        .findLatestByUserId(userId)
        .ifPresent(
            weight ->
                prompt.append(String.format("Peso mas reciente: %.1f kg. ", weight.getWeightKg())));

    String ragContext = ragService.buildContext(userId, userText);
    if (!ragContext.isBlank()) {
      prompt.append(ragContext).append(' ');
    }

    return new AiContextSnapshot(prompt.toString().trim(), true);
  }
}
