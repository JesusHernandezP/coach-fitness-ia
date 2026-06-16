package com.fitnesscoach.dashboard;

import com.fitnesscoach.activity.ActivityLog;
import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.nutrition.FoodLog;
import com.fitnesscoach.nutrition.FoodLogRepository;
import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.NutritionTarget;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.weight.WeightLog;
import com.fitnesscoach.weight.WeightLogRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final WeightLogRepository weightLogRepo;
  private final ActivityLogRepository activityRepo;
  private final MetabolicProfileRepository profileRepo;
  private final NutritionTargetRepository targetRepo;
  private final FoodLogRepository foodLogRepo;

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
    LocalDate date = LocalDate.now();
    ActivityLog todayLog = activityRepo.findByUserIdAndDate(userId, date).orElse(null);
    List<FoodLog> foodLogs = foodLogRepo.findByUserIdAndDateOrderByCreatedAtAsc(userId, date);
    NutritionTarget target = targetRepo.findByUserId(userId).orElse(null);

    int steps = todayLog != null ? nullToZero(todayLog.getSteps()) : 0;
    int calsBurned = todayLog != null ? nullToZero(todayLog.getCaloriesBurned()) : 0;
    Double currentWeight =
        profileRepo.findByUserId(userId).map(p -> p.getCurrentWeightKg()).orElse(null);

    double consumedCalories = sum(foodLogs, FoodLog::getCalories);
    double consumedProtein = sum(foodLogs, FoodLog::getProteinG);

    Double targetCalories = target != null ? target.getCalories() : null;
    Double targetProtein = target != null ? target.getProteinG() : null;

    return new TodaySnapshot(
        targetCalories,
        consumedCalories,
        remaining(targetCalories, consumedCalories),
        targetProtein,
        consumedProtein,
        remaining(targetProtein, consumedProtein),
        steps,
        calsBurned,
        currentWeight,
        todayLog != null ? todayLog.getSource() : null,
        todayLog != null ? todayLog.getSyncedAt() : null);
  }

  public WeeklyKpisSnapshot weeklyKpis(Long userId) {
    LocalDate today = LocalDate.now();
    LocalDate from = today.minusDays(6);
    NutritionTarget target = targetRepo.findByUserId(userId).orElse(null);
    Map<LocalDate, List<FoodLog>> foodLogsByDate = foodLogsByDate(userId, from, today);
    Map<LocalDate, ActivityLog> activitiesByDate = activitiesByDate(userId, from, today);

    int daysWithMealsLogged = 0;
    double totalCaloriesConsumed = 0;
    double totalProteinConsumed = 0;
    long totalSteps = 0;
    long totalCaloriesBurned = 0;
    int daysWithinTarget = 0;

    for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
      List<FoodLog> foodLogs = foodLogsByDate.getOrDefault(date, List.of());
      ActivityLog activity = activitiesByDate.get(date);
      double consumedCalories = sum(foodLogs, FoodLog::getCalories);
      double consumedProtein = sum(foodLogs, FoodLog::getProteinG);

      if (!foodLogs.isEmpty()) {
        daysWithMealsLogged++;
      }

      totalCaloriesConsumed += consumedCalories;
      totalProteinConsumed += consumedProtein;
      totalSteps += activity != null ? nullToZero(activity.getSteps()) : 0;
      totalCaloriesBurned += activity != null ? nullToZero(activity.getCaloriesBurned()) : 0;

      if (target != null
          && target.getCalories() != null
          && target.getCalories() > 0
          && consumedCalories > 0
          && isWithinAdherenceThreshold(consumedCalories, target.getCalories())) {
        daysWithinTarget++;
      }
    }

    Double caloricAdherencePct =
        target != null && target.getCalories() != null ? daysWithinTarget * 100.0 / 7.0 : null;

    return new WeeklyKpisSnapshot(
        daysWithMealsLogged,
        totalCaloriesConsumed / 7.0,
        totalProteinConsumed / 7.0,
        totalSteps / 7.0,
        totalCaloriesBurned,
        computeWeightDelta(userId, from, today),
        caloricAdherencePct,
        computeLoggingStreak(userId, today));
  }

  public List<NutritionTrendPoint> nutritionTrend(Long userId, int days) {
    LocalDate today = LocalDate.now();
    LocalDate from = today.minusDays(safeRange(days) - 1L);
    NutritionTarget target = targetRepo.findByUserId(userId).orElse(null);
    Map<LocalDate, List<FoodLog>> logsByDate = foodLogsByDate(userId, from, today);
    List<NutritionTrendPoint> response = new ArrayList<>();

    for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
      List<FoodLog> logs = logsByDate.getOrDefault(date, List.of());
      response.add(
          new NutritionTrendPoint(
              date,
              sum(logs, FoodLog::getCalories),
              target != null ? target.getCalories() : null,
              sum(logs, FoodLog::getProteinG),
              target != null ? target.getProteinG() : null));
    }

    return response;
  }

  public List<ActivityTrendPoint> activityTrend(Long userId, int days) {
    LocalDate today = LocalDate.now();
    LocalDate from = today.minusDays(safeRange(days) - 1L);
    Map<LocalDate, ActivityLog> activities = activitiesByDate(userId, from, today);
    List<ActivityTrendPoint> response = new ArrayList<>();

    for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
      ActivityLog log = activities.get(date);
      response.add(
          new ActivityTrendPoint(
              date,
              log != null ? nullToZero(log.getSteps()) : 0,
              log != null ? nullToZero(log.getCaloriesBurned()) : 0));
    }

    return response;
  }

  public List<AdherencePoint> adherence(Long userId, int days) {
    LocalDate today = LocalDate.now();
    LocalDate from = today.minusDays(safeRange(days) - 1L);
    NutritionTarget target = targetRepo.findByUserId(userId).orElse(null);
    Map<LocalDate, List<FoodLog>> logsByDate = foodLogsByDate(userId, from, today);
    List<AdherencePoint> response = new ArrayList<>();

    for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
      double consumedCalories = sum(logsByDate.getOrDefault(date, List.of()), FoodLog::getCalories);
      Double targetCalories = target != null ? target.getCalories() : null;
      response.add(
          new AdherencePoint(
              date,
              consumedCalories,
              targetCalories,
              adherencePct(consumedCalories, targetCalories)));
    }

    return response;
  }

  private Double computeWeightDelta(Long userId, LocalDate from, LocalDate to) {
    Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<WeightLog> logs =
        weightLogRepo.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            userId, fromInstant, toInstant);

    if (logs.size() < 2) return null;
    return logs.get(logs.size() - 1).getWeightKg() - logs.get(0).getWeightKg();
  }

  private int nullToZero(Integer value) {
    return value != null ? value : 0;
  }

  private double sum(List<FoodLog> logs, java.util.function.Function<FoodLog, Double> extractor) {
    return logs.stream()
        .map(extractor)
        .filter(v -> v != null)
        .mapToDouble(Double::doubleValue)
        .sum();
  }

  private Double remaining(Double target, double consumed) {
    return target != null ? target - consumed : null;
  }

  private Map<LocalDate, List<FoodLog>> foodLogsByDate(Long userId, LocalDate from, LocalDate to) {
    Map<LocalDate, List<FoodLog>> grouped = new HashMap<>();
    for (FoodLog log :
        foodLogRepo.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(userId, from, to)) {
      grouped.computeIfAbsent(log.getDate(), ignored -> new ArrayList<>()).add(log);
    }
    return grouped;
  }

  private Map<LocalDate, ActivityLog> activitiesByDate(Long userId, LocalDate from, LocalDate to) {
    Map<LocalDate, ActivityLog> grouped = new HashMap<>();
    for (ActivityLog log :
        activityRepo.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)) {
      grouped.put(log.getDate(), log);
    }
    return grouped;
  }

  private boolean isWithinAdherenceThreshold(double consumedCalories, double targetCalories) {
    double deviationPct = Math.abs(consumedCalories - targetCalories) / targetCalories;
    return deviationPct <= 0.1;
  }

  private Double adherencePct(double consumedCalories, Double targetCalories) {
    if (targetCalories == null || targetCalories <= 0 || consumedCalories <= 0) {
      return null;
    }
    double deviationPct = Math.abs(consumedCalories - targetCalories) / targetCalories;
    double score = Math.max(0, 100 - (deviationPct * 100));
    return Math.min(score, 100);
  }

  private int computeLoggingStreak(Long userId, LocalDate today) {
    Map<LocalDate, List<FoodLog>> foodLogs = foodLogsByDate(userId, today.minusDays(29), today);
    int streak = 0;
    for (LocalDate date = today;
        foodLogs.containsKey(date) && !foodLogs.get(date).isEmpty();
        date = date.minusDays(1)) {
      streak++;
    }
    return streak;
  }

  private int safeRange(int days) {
    return Math.max(1, days);
  }
}
