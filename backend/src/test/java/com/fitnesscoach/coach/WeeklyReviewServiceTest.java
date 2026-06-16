package com.fitnesscoach.coach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnesscoach.activity.ActivityLog;
import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.chat.GroqClient;
import com.fitnesscoach.dashboard.DashboardService;
import com.fitnesscoach.dashboard.WeeklyKpisSnapshot;
import com.fitnesscoach.nutrition.FoodLog;
import com.fitnesscoach.nutrition.FoodLogRepository;
import com.fitnesscoach.nutrition.FoodLogSource;
import com.fitnesscoach.nutrition.MealType;
import com.fitnesscoach.profile.ActivityLevel;
import com.fitnesscoach.profile.DietType;
import com.fitnesscoach.profile.Goal;
import com.fitnesscoach.profile.MetabolicProfile;
import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.NutritionTarget;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.profile.Sex;
import com.fitnesscoach.weight.WeightLog;
import com.fitnesscoach.weight.WeightLogRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeeklyReviewServiceTest {

  @Mock MetabolicProfileRepository profileRepository;
  @Mock NutritionTargetRepository targetRepository;
  @Mock FoodLogRepository foodLogRepository;
  @Mock ActivityLogRepository activityLogRepository;
  @Mock WeightLogRepository weightLogRepository;
  @Mock DashboardService dashboardService;
  @Mock GroqClient groqClient;

  private WeeklyReviewService weeklyReviewService;

  @BeforeEach
  void setUp() {
    weeklyReviewService =
        new WeeklyReviewService(
            profileRepository,
            targetRepository,
            foodLogRepository,
            activityLogRepository,
            weightLogRepository,
            dashboardService,
            groqClient,
            new ObjectMapper());
  }

  @Test
  void generatesReviewFromAiJsonWhenDataExists() {
    when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile()));
    when(targetRepository.findByUserId(1L)).thenReturn(Optional.of(target()));
    when(foodLogRepository.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(
            any(), any(), any()))
        .thenReturn(List.of(foodLog(LocalDate.now(), 620, 42)));
    when(activityLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
        .thenReturn(
            List.of(
                ActivityLog.builder()
                    .date(LocalDate.now())
                    .steps(9000)
                    .caloriesBurned(380)
                    .build()));
    when(weightLogRepository.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(any(), any(), any()))
        .thenReturn(List.of(weightLog(81.2)));
    when(dashboardService.weeklyKpis(1L))
        .thenReturn(new WeeklyKpisSnapshot(5, 2100, 145, 8200, 2300, -0.4, 71.0, 4));
    when(groqClient.complete(any()))
        .thenReturn(
            """
            {
              "summary":"Esta semana cumpliste 5 de 7 dias con registros utiles.",
              "nutritionFindings":["Proteina media ligeramente por debajo del objetivo."],
              "activityFindings":["Pasos consistentes durante cuatro dias."],
              "weightFindings":["El peso bajo 0.4 kg."],
              "recommendations":["Sube proteina en desayuno.","Mantiene calorias objetivo."],
              "riskNotes":["Las estimaciones de comidas pueden tener margen de error."]
            }
            """);

    WeeklyReviewResponse response = weeklyReviewService.generate(1L);

    assertThat(response.summary()).contains("5 de 7");
    assertThat(response.nutritionFindings())
        .containsExactly("Proteina media ligeramente por debajo del objetivo.");
    assertThat(response.recommendations()).contains("Sube proteina en desayuno.");
  }

  @Test
  void fallsBackGracefullyWhenAiReturnsInvalidJsonAndDataIsSparse() {
    when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
    when(targetRepository.findByUserId(1L)).thenReturn(Optional.empty());
    when(foodLogRepository.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(
            any(), any(), any()))
        .thenReturn(List.of());
    when(activityLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
        .thenReturn(List.of());
    when(weightLogRepository.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(any(), any(), any()))
        .thenReturn(List.of());
    when(dashboardService.weeklyKpis(1L))
        .thenReturn(new WeeklyKpisSnapshot(0, 0, 0, 0, 0, null, null, 0));
    when(groqClient.complete(any())).thenReturn("sin json");

    WeeklyReviewResponse response = weeklyReviewService.generate(1L);

    assertThat(response.summary()).contains("Hay pocos datos");
    assertThat(response.activityFindings())
        .containsExactly("No hay actividad registrada esta semana.");
    assertThat(response.weightFindings())
        .containsExactly("No hay suficientes pesos para calcular el cambio semanal.");
  }

  private MetabolicProfile profile() {
    return MetabolicProfile.builder()
        .age(30)
        .sex(Sex.MALE)
        .heightCm(175.0)
        .currentWeightKg(81.2)
        .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
        .dietType(DietType.STANDARD)
        .goal(Goal.LOSE_WEIGHT)
        .build();
  }

  private NutritionTarget target() {
    return NutritionTarget.builder()
        .calories(2360.0)
        .proteinG(160.0)
        .carbsG(286.0)
        .fatG(64.0)
        .build();
  }

  private FoodLog foodLog(LocalDate date, double calories, double proteinG) {
    return FoodLog.builder()
        .date(date)
        .mealType(MealType.lunch)
        .description("Arroz con pollo")
        .calories(calories)
        .proteinG(proteinG)
        .source(FoodLogSource.manual)
        .createdAt(Instant.now())
        .build();
  }

  private WeightLog weightLog(double weightKg) {
    return WeightLog.builder()
        .weightKg(weightKg)
        .loggedAt(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC))
        .build();
  }
}
