package com.fitnesscoach.dashboard;

import com.fitnesscoach.activity.ActivityLog;
import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.weight.WeightLog;
import com.fitnesscoach.weight.WeightLogRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final WeightLogRepository weightLogRepo;
  private final ActivityLogRepository activityRepo;
  private final MetabolicProfileRepository profileRepo;
  private final NutritionTargetRepository targetRepo;

  public List<WeightProgressPoint> weightProgress(Long userId, int days) {
    Instant from = Instant.now().minus(days, ChronoUnit.DAYS);
    return weightLogRepo
        .findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, from, Instant.now())
        .stream()
        .map(w -> new WeightProgressPoint(w.getLoggedAt(), w.getWeightKg()))
        .toList();
  }

  public WeeklySummary weeklySummary(Long userId) {
    LocalDate today = LocalDate.now();
    LocalDate weekAgo = today.minusDays(6);

    List<ActivityLog> logs =
        activityRepo.findByUserIdAndDateBetweenOrderByDateAsc(userId, weekAgo, today);

    long stepsTotal = logs.stream().mapToLong(a -> nullToZero(a.getSteps())).sum();
    long calsTotal = logs.stream().mapToLong(a -> nullToZero(a.getCaloriesBurned())).sum();
    int daysLogged = logs.size();
    double avgSteps = daysLogged > 0 ? (double) stepsTotal / daysLogged : 0;

    Double weightDelta = computeWeightDelta(userId, weekAgo, today);

    return new WeeklySummary(daysLogged, stepsTotal, calsTotal, avgSteps, weightDelta);
  }

  public TodaySnapshot today(Long userId) {
    ActivityLog todayLog =
        activityRepo.findByUserIdAndDate(userId, LocalDate.now()).orElse(null);

    int steps = todayLog != null ? nullToZero(todayLog.getSteps()) : 0;
    int cals = todayLog != null ? nullToZero(todayLog.getCaloriesBurned()) : 0;

    Double currentWeight =
        profileRepo.findByUserId(userId).map(p -> p.getCurrentWeightKg()).orElse(null);

    Double targetCalories =
        targetRepo.findByUserId(userId).map(t -> t.getCalories()).orElse(null);

    return new TodaySnapshot(steps, cals, currentWeight, targetCalories);
  }

  private Double computeWeightDelta(Long userId, LocalDate from, LocalDate to) {
    Instant fromInstant = from.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
    Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);

    List<WeightLog> logs =
        weightLogRepo.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            userId, fromInstant, toInstant);

    if (logs.size() < 2) return null;
    return logs.get(logs.size() - 1).getWeightKg() - logs.get(0).getWeightKg();
  }

  private int nullToZero(Integer value) {
    return value != null ? value : 0;
  }
}
