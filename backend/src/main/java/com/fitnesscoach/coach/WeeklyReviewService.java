package com.fitnesscoach.coach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnesscoach.activity.ActivityLog;
import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.chat.GroqClient;
import com.fitnesscoach.dashboard.WeeklyKpisSnapshot;
import com.fitnesscoach.dashboard.DashboardService;
import com.fitnesscoach.nutrition.FoodLog;
import com.fitnesscoach.nutrition.FoodLogRepository;
import com.fitnesscoach.profile.MetabolicProfile;
import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.NutritionTarget;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.weight.WeightLog;
import com.fitnesscoach.weight.WeightLogRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeeklyReviewService {

  private static final String SYSTEM_PROMPT =
      "Eres un coach personal con rol de nutricionista y entrenador. "
          + "Debes analizar solo los datos proporcionados. "
          + "No inventes pesos, comidas ni actividad. "
          + "No des diagnosticos medicos. "
          + "Responde solo JSON valido con las claves: summary, nutritionFindings, activityFindings, weightFindings, recommendations, riskNotes.";

  private final MetabolicProfileRepository profileRepository;
  private final NutritionTargetRepository targetRepository;
  private final FoodLogRepository foodLogRepository;
  private final ActivityLogRepository activityLogRepository;
  private final WeightLogRepository weightLogRepository;
  private final DashboardService dashboardService;
  private final GroqClient groqClient;
  private final ObjectMapper objectMapper;

  public WeeklyReviewResponse generate(Long userId) {
    LocalDate periodEnd = LocalDate.now();
    LocalDate periodStart = periodEnd.minusDays(6);

    MetabolicProfile profile = profileRepository.findByUserId(userId).orElse(null);
    NutritionTarget target = targetRepository.findByUserId(userId).orElse(null);
    List<FoodLog> foods =
        foodLogRepository.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(
            userId, periodStart, periodEnd);
    List<ActivityLog> activities =
        activityLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, periodStart, periodEnd);
    List<WeightLog> weights =
        weightLogRepository.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            userId,
            periodStart.atStartOfDay().toInstant(ZoneOffset.UTC),
            periodEnd.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
    WeeklyKpisSnapshot weeklyKpis = dashboardService.weeklyKpis(userId);

    String prompt = buildPrompt(periodStart, periodEnd, profile, target, foods, activities, weights, weeklyKpis);

    try {
      String raw =
          groqClient.complete(
              List.of(
                  Map.of("role", "system", "content", SYSTEM_PROMPT),
                  Map.of("role", "user", "content", prompt)));
      AiWeeklyReviewPayload payload =
          objectMapper.readValue(extractJson(raw), AiWeeklyReviewPayload.class);
      return payload.toResponse(periodStart, periodEnd);
    } catch (Exception ignored) {
      return fallback(periodStart, periodEnd, weeklyKpis, foods, activities, weights, target);
    }
  }

  private String buildPrompt(
      LocalDate periodStart,
      LocalDate periodEnd,
      MetabolicProfile profile,
      NutritionTarget target,
      List<FoodLog> foods,
      List<ActivityLog> activities,
      List<WeightLog> weights,
      WeeklyKpisSnapshot weeklyKpis) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Revision semanal del ")
        .append(periodStart)
        .append(" al ")
        .append(periodEnd)
        .append(". ");

    if (profile != null) {
      prompt.append("Perfil: objetivo ")
          .append(profile.getGoal())
          .append(", dieta ")
          .append(profile.getDietType())
          .append(", actividad ")
          .append(profile.getActivityLevel())
          .append(", peso actual ")
          .append(profile.getCurrentWeightKg())
          .append(" kg. ");
    } else {
      prompt.append("Perfil no disponible. ");
    }

    if (target != null) {
      prompt.append(
          String.format(
              Locale.US,
              "Objetivos: %.0f kcal, %.0f g proteina, %.0f g carbs, %.0f g grasa. ",
              safe(target.getCalories()),
              safe(target.getProteinG()),
              safe(target.getCarbsG()),
              safe(target.getFatG())));
    } else {
      prompt.append("Objetivos nutricionales no disponibles. ");
    }

    prompt.append(
        String.format(
            Locale.US,
            "KPIs: comidas en %d/7 dias, %.0f kcal medias, %.0f g proteina media, %.0f pasos medios, %d kcal activas totales, delta peso %.1f kg, adherencia %.0f%%, racha %d dias. ",
            weeklyKpis.daysWithMealsLogged(),
            weeklyKpis.avgCaloriesConsumed(),
            weeklyKpis.avgProteinConsumed(),
            weeklyKpis.avgSteps(),
            weeklyKpis.activeCaloriesTotal(),
            safe(weeklyKpis.weightDelta()),
            safe(weeklyKpis.caloricAdherencePct()),
            weeklyKpis.loggingStreakDays()));

    prompt.append("Comidas registradas: ");
    if (foods.isEmpty()) {
      prompt.append("ninguna. ");
    } else {
      foods.stream()
          .limit(20)
          .forEach(
              food ->
                  prompt.append(
                      String.format(
                          Locale.US,
                          "[%s %s: %s, %.0f kcal, %.0f g proteina] ",
                          food.getDate(),
                          food.getMealType(),
                          food.getDescription(),
                          food.getCalories(),
                          safe(food.getProteinG()))));
    }

    prompt.append("Actividad registrada: ");
    if (activities.isEmpty()) {
      prompt.append("ninguna. ");
    } else {
      activities.forEach(
          activity ->
              prompt.append(
                  String.format(
                      Locale.US,
                      "[%s: %d pasos, %d kcal] ",
                      activity.getDate(),
                      zero(activity.getSteps()),
                      zero(activity.getCaloriesBurned()))));
    }

    prompt.append("Pesos registrados: ");
    if (weights.isEmpty()) {
      prompt.append("ninguno. ");
    } else {
      weights.forEach(
          weight ->
              prompt.append(
                  String.format(
                      Locale.US,
                      "[%s: %.1f kg] ",
                      weight.getLoggedAt().atOffset(ZoneOffset.UTC).toLocalDate(),
                      weight.getWeightKg())));
    }

    prompt.append(
        "Si faltan datos, dilo expresamente en summary o riskNotes. "
            + "Recommendations debe tener recomendaciones accionables y breves.");
    return prompt.toString();
  }

  private WeeklyReviewResponse fallback(
      LocalDate periodStart,
      LocalDate periodEnd,
      WeeklyKpisSnapshot weeklyKpis,
      List<FoodLog> foods,
      List<ActivityLog> activities,
      List<WeightLog> weights,
      NutritionTarget target) {
    String summary =
        String.format(
            Locale.US,
            "Semana del %s al %s con %d de 7 dias con comidas registradas y %.0f pasos medios. %s",
            periodStart,
            periodEnd,
            weeklyKpis.daysWithMealsLogged(),
            weeklyKpis.avgSteps(),
            foods.isEmpty() && activities.isEmpty() && weights.isEmpty()
                ? "Hay pocos datos para una revision precisa."
                : "La revision usa solo los registros disponibles.");

    List<String> nutritionFindings =
        List.of(
            target != null && weeklyKpis.avgProteinConsumed() < safe(target.getProteinG()) * 0.8
                ? "La proteina media semanal quedo por debajo del objetivo."
                : foods.isEmpty()
                    ? "No hay suficientes comidas registradas para analizar nutricion."
                    : "La nutricion semanal refleja solo las comidas registradas.");

    List<String> activityFindings =
        List.of(
            activities.isEmpty()
                ? "No hay actividad registrada esta semana."
                : String.format(
                    Locale.US,
                    "Media diaria de %.0f pasos y %d kcal activas totales.",
                    weeklyKpis.avgSteps(), weeklyKpis.activeCaloriesTotal()));

    List<String> weightFindings =
        List.of(
            weeklyKpis.weightDelta() == null
                ? "No hay suficientes pesos para calcular el cambio semanal."
                : String.format(
                    Locale.US, "El peso vario %.1f kg durante la semana.", weeklyKpis.weightDelta()));

    List<String> recommendations =
        List.of(
            weeklyKpis.daysWithMealsLogged() < 4
                ? "Registra mas comidas para mejorar la precision del seguimiento."
                : "Mantiene un registro diario consistente.",
            target != null && weeklyKpis.avgProteinConsumed() < safe(target.getProteinG()) * 0.8
                ? "Sube la proteina en desayuno o comida principal."
                : "Sostiene las calorias objetivo con consistencia.");

    List<String> riskNotes =
        List.of(
            "Las estimaciones de comidas pueden tener margen de error.",
            foods.isEmpty() || activities.isEmpty()
                ? "Faltan registros en parte de la semana, asi que la revision es parcial."
                : "La revision depende de la calidad de los datos registrados.");

    return new WeeklyReviewResponse(
        periodStart,
        periodEnd,
        summary,
        nutritionFindings,
        activityFindings,
        weightFindings,
        recommendations,
        riskNotes);
  }

  private String extractJson(String raw) {
    int start = raw.indexOf('{');
    int end = raw.lastIndexOf('}');
    if (start < 0 || end < start) {
      throw new IllegalArgumentException("Respuesta IA sin JSON valido");
    }
    return raw.substring(start, end + 1);
  }

  private double safe(Double value) {
    return value != null ? value : 0.0;
  }

  private int zero(Integer value) {
    return value != null ? value : 0;
  }

  record AiWeeklyReviewPayload(
      String summary,
      List<String> nutritionFindings,
      List<String> activityFindings,
      List<String> weightFindings,
      List<String> recommendations,
      List<String> riskNotes) {

    WeeklyReviewResponse toResponse(LocalDate periodStart, LocalDate periodEnd) {
      return new WeeklyReviewResponse(
          periodStart,
          periodEnd,
          summary != null ? summary : "",
          nutritionFindings != null ? nutritionFindings : List.of(),
          activityFindings != null ? activityFindings : List.of(),
          weightFindings != null ? weightFindings : List.of(),
          recommendations != null ? recommendations : List.of(),
          riskNotes != null ? riskNotes : List.of());
    }
  }
}
