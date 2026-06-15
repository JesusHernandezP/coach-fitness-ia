package com.fitnesscoach.nutrition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitnesscoach.activity.ActivityLog;
import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.profile.NutritionTarget;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.user.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodLogServiceTest {

  @Mock FoodLogRepository foodLogRepository;
  @Mock NutritionTargetRepository nutritionTargetRepository;
  @Mock ActivityLogRepository activityLogRepository;

  @InjectMocks FoodLogService foodLogService;

  private final User user = User.builder().id(1L).email("test@test.com").build();

  @Test
  void create_persistsManualFoodLog() {
    FoodLogRequest request =
        new FoodLogRequest(
            LocalDate.of(2026, 6, 15),
            MealType.breakfast,
            "Avena con yogur",
            430.0,
            26.0,
            48.0,
            12.0,
            FoodLogSource.manual,
            null);
    when(foodLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FoodLogResponse response = foodLogService.create(user, request);

    assertThat(response.description()).isEqualTo("Avena con yogur");
    assertThat(response.calories()).isEqualTo(430.0);
    assertThat(response.mealType()).isEqualTo(MealType.breakfast);
  }

  @Test
  void update_rejectsLogFromAnotherUser() {
    when(foodLogRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                foodLogService.update(
                    99L,
                    1L,
                    new FoodLogRequest(
                        LocalDate.now(),
                        MealType.lunch,
                        "Bowl",
                        500.0,
                        null,
                        null,
                        null,
                        FoodLogSource.manual,
                        null)))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Comida no encontrada");
  }

  @Test
  void todaySummary_calculatesConsumedRemainingAndNetCalories() {
    LocalDate today = LocalDate.now();
    when(foodLogRepository.findByUserIdAndDateOrderByCreatedAtAsc(1L, today))
        .thenReturn(
            List.of(
                FoodLog.builder().calories(500.0).proteinG(35.0).carbsG(50.0).fatG(15.0).build(),
                FoodLog.builder().calories(780.0).proteinG(57.0).carbsG(90.0).fatG(26.0).build()));
    when(nutritionTargetRepository.findByUserId(1L))
        .thenReturn(
            Optional.of(
                NutritionTarget.builder()
                    .calories(2360.0)
                    .proteinG(160.0)
                    .carbsG(286.0)
                    .fatG(64.0)
                    .build()));
    when(activityLogRepository.findByUserIdAndDate(1L, today))
        .thenReturn(Optional.of(ActivityLog.builder().caloriesBurned(430).build()));

    DailyNutritionSummary summary = foodLogService.todaySummary(1L);

    assertThat(summary.consumedCalories()).isEqualTo(1280.0);
    assertThat(summary.remainingCalories()).isEqualTo(1080.0);
    assertThat(summary.consumedProteinG()).isEqualTo(92.0);
    assertThat(summary.remainingProteinG()).isEqualTo(68.0);
    assertThat(summary.activityCaloriesBurned()).isEqualTo(430);
    assertThat(summary.netCalories()).isEqualTo(850.0);
  }

  @Test
  void weeklySummary_returnsSevenDaysIncludingEmptyDays() {
    LocalDate today = LocalDate.now();
    LocalDate from = today.minusDays(6);
    when(foodLogRepository.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(1L, from, today))
        .thenReturn(
            List.of(
                FoodLog.builder().date(from).calories(400.0).build(),
                FoodLog.builder().date(today).calories(900.0).build()));
    when(nutritionTargetRepository.findByUserId(1L)).thenReturn(Optional.empty());
    when(activityLogRepository.findByUserIdAndDate(any(), any())).thenReturn(Optional.empty());

    NutritionWeeklySummaryResponse response = foodLogService.weeklySummary(1L);

    assertThat(response.days()).hasSize(7);
    assertThat(response.days().get(0).date()).isEqualTo(from);
    assertThat(response.days().get(0).consumedCalories()).isEqualTo(400.0);
    assertThat(response.days().get(6).date()).isEqualTo(today);
    assertThat(response.days().get(6).consumedCalories()).isEqualTo(900.0);
  }

  @Test
  void delete_removesOwnedFoodLog() {
    FoodLog log = FoodLog.builder().id(7L).user(user).build();
    when(foodLogRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(log));

    foodLogService.delete(7L, 1L);

    verify(foodLogRepository).delete(log);
  }
}
