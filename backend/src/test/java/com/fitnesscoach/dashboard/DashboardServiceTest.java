package com.fitnesscoach.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fitnesscoach.activity.ActivityLog;
import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.profile.MetabolicProfileRepository;
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

    TodaySnapshot snap = dashboardService.today(1L);

    assertThat(snap.steps()).isEqualTo(0);
    assertThat(snap.caloriesBurned()).isEqualTo(0);
    assertThat(snap.currentWeightKg()).isNull();
  }

  private ActivityLog activityLog(int steps, int cals) {
    return ActivityLog.builder().date(LocalDate.now()).steps(steps).caloriesBurned(cals).build();
  }

  private WeightLog weightLog(double kg, Instant at) {
    return WeightLog.builder().weightKg(kg).loggedAt(at).build();
  }
}
