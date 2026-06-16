package com.fitnesscoach.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fitnesscoach.activity.ActivityLog;
import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.nutrition.FoodLog;
import com.fitnesscoach.nutrition.FoodLogRepository;
import com.fitnesscoach.nutrition.FoodLogSource;
import com.fitnesscoach.nutrition.MealType;
import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.NutritionTarget;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.weight.WeightLog;
import com.fitnesscoach.weight.WeightLogRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock WeightLogRepository weightLogRepo;
  @Mock ActivityLogRepository activityRepo;
  @Mock MetabolicProfileRepository profileRepo;
  @Mock NutritionTargetRepository targetRepo;
  @Mock FoodLogRepository foodLogRepo;

  @InjectMocks DashboardService dashboardService;

  @Test
  void weeklySummary_aggregatesCorrectly() {
    List<ActivityLog> logs =
        List.of(activityLog(8000, 300), activityLog(10000, 400), activityLog(6000, 250));

    when(activityRepo.findByUserIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
        .thenReturn(logs);
    when(weightLogRepo.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(eq(1L), any(), any()))
        .thenReturn(List.of());

    WeeklySummary summary = dashboardService.weeklySummary(1L);

    assertThat(summary.daysLogged()).isEqualTo(3);
    assertThat(summary.stepsTotal()).isEqualTo(24000);
    assertThat(summary.caloriesBurnedTotal()).isEqualTo(950);
    assertThat(summary.avgSteps()).isEqualTo(8000.0);
    assertThat(summary.weightDelta()).isNull();
  }

  @Test
  void weeklySummary_computesWeightDelta_whenTwoOrMoreLogs() {
    when(activityRepo.findByUserIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
        .thenReturn(List.of());
    when(weightLogRepo.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(eq(1L), any(), any()))
        .thenReturn(
            List.of(
                weightLog(80.0, Instant.now().minusSeconds(86400 * 6)),
                weightLog(79.2, Instant.now())));

    WeeklySummary summary = dashboardService.weeklySummary(1L);

    assertThat(summary.weightDelta()).isCloseTo(-0.8, within(0.001));
  }

  @Test
  void today_returnsZeros_whenNoActivityLog() {
    when(activityRepo.findByUserIdAndDate(eq(1L), any())).thenReturn(Optional.empty());
    when(profileRepo.findByUserId(1L)).thenReturn(Optional.empty());
    when(targetRepo.findByUserId(1L)).thenReturn(Optional.empty());
    when(foodLogRepo.findByUserIdAndDateOrderByCreatedAtAsc(eq(1L), any())).thenReturn(List.of());

    TodaySnapshot snap = dashboardService.today(1L);

    assertThat(snap.targetCalories()).isNull();
    assertThat(snap.consumedCalories()).isZero();
    assertThat(snap.remainingCalories()).isNull();
    assertThat(snap.targetProteinG()).isNull();
    assertThat(snap.consumedProteinG()).isZero();
    assertThat(snap.remainingProteinG()).isNull();
    assertThat(snap.steps()).isEqualTo(0);
    assertThat(snap.caloriesBurned()).isEqualTo(0);
    assertThat(snap.currentWeightKg()).isNull();
    assertThat(snap.activitySource()).isNull();
  }

  @Test
  void today_includes_nutrition_kpis_when_food_and_target_exist() {
    when(activityRepo.findByUserIdAndDate(eq(1L), any()))
        .thenReturn(Optional.of(activityLog(7200, 380)));
    when(foodLogRepo.findByUserIdAndDateOrderByCreatedAtAsc(eq(1L), any()))
        .thenReturn(List.of(foodLog(LocalDate.now(), 600, 40), foodLog(LocalDate.now(), 850, 55)));
    when(targetRepo.findByUserId(1L))
        .thenReturn(
            Optional.of(NutritionTarget.builder().calories(2360.0).proteinG(160.0).build()));

    TodaySnapshot snap = dashboardService.today(1L);

    assertThat(snap.targetCalories()).isEqualTo(2360.0);
    assertThat(snap.consumedCalories()).isEqualTo(1450.0);
    assertThat(snap.remainingCalories()).isEqualTo(910.0);
    assertThat(snap.targetProteinG()).isEqualTo(160.0);
    assertThat(snap.consumedProteinG()).isEqualTo(95.0);
    assertThat(snap.remainingProteinG()).isEqualTo(65.0);
    assertThat(snap.steps()).isEqualTo(7200);
    assertThat(snap.caloriesBurned()).isEqualTo(380);
  }

  @Test
  void weeklyKpis_aggregates_multiple_days_and_streak() {
    LocalDate today = LocalDate.now();
    LocalDate weekAgo = today.minusDays(6);
    when(targetRepo.findByUserId(1L))
        .thenReturn(
            Optional.of(NutritionTarget.builder().calories(2000.0).proteinG(150.0).build()));
    when(activityRepo.findByUserIdAndDateBetweenOrderByDateAsc(eq(1L), any(), any()))
        .thenReturn(
            List.of(
                ActivityLog.builder().date(weekAgo).steps(8000).caloriesBurned(300).build(),
                ActivityLog.builder()
                    .date(weekAgo.plusDays(1))
                    .steps(6000)
                    .caloriesBurned(250)
                    .build(),
                ActivityLog.builder().date(today).steps(10000).caloriesBurned(450).build()));
    when(foodLogRepo.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(eq(1L), any(), any()))
        .thenReturn(
            List.of(
                foodLog(today.minusDays(2), 1900, 120),
                foodLog(today.minusDays(1), 2050, 140),
                foodLog(today, 1980, 160)));
    when(weightLogRepo.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(eq(1L), any(), any()))
        .thenReturn(
            List.of(
                weightLog(81.0, weekAgo.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)),
                weightLog(80.4, today.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))));

    WeeklyKpisSnapshot snapshot = dashboardService.weeklyKpis(1L);

    assertThat(snapshot.daysWithMealsLogged()).isEqualTo(3);
    assertThat(snapshot.avgCaloriesConsumed()).isCloseTo(847.14, within(0.01));
    assertThat(snapshot.avgProteinConsumed()).isCloseTo(60.0, within(0.01));
    assertThat(snapshot.avgSteps()).isCloseTo(3428.57, within(0.01));
    assertThat(snapshot.activeCaloriesTotal()).isEqualTo(1000);
    assertThat(snapshot.weightDelta()).isCloseTo(-0.6, within(0.001));
    assertThat(snapshot.caloricAdherencePct()).isCloseTo(42.857, within(0.01));
    assertThat(snapshot.loggingStreakDays()).isEqualTo(3);
  }

  @Test
  void adherence_returns_null_when_target_missing_and_zero_filled_series() {
    LocalDate today = LocalDate.now();
    when(targetRepo.findByUserId(1L)).thenReturn(Optional.empty());
    when(foodLogRepo.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(eq(1L), any(), any()))
        .thenReturn(List.of(foodLog(today.minusDays(1), 1500, 80)));

    List<AdherencePoint> points = dashboardService.adherence(1L, 3);

    assertThat(points).hasSize(3);
    assertThat(points.get(0).date()).isEqualTo(today.minusDays(2));
    assertThat(points.get(0).consumedCalories()).isZero();
    assertThat(points.get(0).adherencePct()).isNull();
    assertThat(points.get(1).consumedCalories()).isEqualTo(1500.0);
    assertThat(points.get(1).adherencePct()).isNull();
    assertThat(points.get(2).date()).isEqualTo(today);
  }

  private ActivityLog activityLog(int steps, int cals) {
    return ActivityLog.builder().date(LocalDate.now()).steps(steps).caloriesBurned(cals).build();
  }

  private WeightLog weightLog(double kg, Instant at) {
    return WeightLog.builder().weightKg(kg).loggedAt(at).build();
  }

  private FoodLog foodLog(LocalDate date, double calories, double proteinG) {
    return FoodLog.builder()
        .date(date)
        .mealType(MealType.lunch)
        .description("meal")
        .calories(calories)
        .proteinG(proteinG)
        .source(FoodLogSource.manual)
        .createdAt(Instant.now())
        .build();
  }
}
