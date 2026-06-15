package com.fitnesscoach.nutrition;

import com.fitnesscoach.activity.ActivityLogRepository;
import com.fitnesscoach.profile.NutritionTarget;
import com.fitnesscoach.profile.NutritionTargetRepository;
import com.fitnesscoach.user.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FoodLogService {

  private final FoodLogRepository foodLogRepository;
  private final NutritionTargetRepository nutritionTargetRepository;
  private final ActivityLogRepository activityLogRepository;

  @Transactional
  public FoodLogResponse create(User user, FoodLogRequest request) {
    FoodLog log = FoodLog.builder().user(user).build();
    apply(log, request);
    return FoodLogResponse.from(foodLogRepository.save(log));
  }

  public List<FoodLogResponse> list(Long userId, LocalDate from, LocalDate to) {
    LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
    LocalDate resolvedTo = to != null ? to : LocalDate.now();
    return foodLogRepository.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(userId, resolvedFrom, resolvedTo).stream()
        .map(FoodLogResponse::from)
        .toList();
  }

  public List<FoodLogResponse> listToday(Long userId) {
    return foodLogRepository.findByUserIdAndDateOrderByCreatedAtAsc(userId, LocalDate.now()).stream()
        .map(FoodLogResponse::from)
        .toList();
  }

  @Transactional
  public FoodLogResponse update(Long id, Long userId, FoodLogRequest request) {
    FoodLog log = resolveOwnedLog(id, userId);
    apply(log, request);
    return FoodLogResponse.from(foodLogRepository.save(log));
  }

  @Transactional
  public void delete(Long id, Long userId) {
    foodLogRepository.delete(resolveOwnedLog(id, userId));
  }

  public DailyNutritionSummary todaySummary(Long userId) {
    return summarize(userId, LocalDate.now());
  }

  public NutritionWeeklySummaryResponse weeklySummary(Long userId) {
    LocalDate today = LocalDate.now();
    LocalDate from = today.minusDays(6);
    Map<LocalDate, List<FoodLog>> logsByDate =
        foodLogRepository.findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(userId, from, today).stream()
            .collect(Collectors.groupingBy(FoodLog::getDate));

    List<DailyNutritionSummary> days =
        from.datesUntil(today.plusDays(1))
            .map(date -> summarize(userId, date, logsByDate.getOrDefault(date, List.of())))
            .toList();

    return new NutritionWeeklySummaryResponse(days);
  }

  private DailyNutritionSummary summarize(Long userId, LocalDate date) {
    List<FoodLog> logs = foodLogRepository.findByUserIdAndDateOrderByCreatedAtAsc(userId, date);
    return summarize(userId, date, logs);
  }

  private DailyNutritionSummary summarize(Long userId, LocalDate date, List<FoodLog> logs) {
    double consumedCalories = sum(logs, FoodLog::getCalories);
    double consumedProtein = sum(logs, FoodLog::getProteinG);
    double consumedCarbs = sum(logs, FoodLog::getCarbsG);
    double consumedFat = sum(logs, FoodLog::getFatG);

    NutritionTarget target = nutritionTargetRepository.findByUserId(userId).orElse(null);
    Double targetCalories = target != null ? target.getCalories() : null;
    Double targetProtein = target != null ? target.getProteinG() : null;
    Double targetCarbs = target != null ? target.getCarbsG() : null;
    Double targetFat = target != null ? target.getFatG() : null;

    int activityCalories =
        activityLogRepository.findByUserIdAndDate(userId, date).map(log -> zero(log.getCaloriesBurned())).orElse(0);

    return new DailyNutritionSummary(
        date,
        targetCalories,
        consumedCalories,
        remaining(targetCalories, consumedCalories),
        targetProtein,
        consumedProtein,
        remaining(targetProtein, consumedProtein),
        targetCarbs,
        consumedCarbs,
        remaining(targetCarbs, consumedCarbs),
        targetFat,
        consumedFat,
        remaining(targetFat, consumedFat),
        activityCalories,
        consumedCalories - activityCalories);
  }

  private void apply(FoodLog log, FoodLogRequest request) {
    log.setDate(request.date());
    log.setMealType(request.mealType());
    log.setDescription(request.description().trim());
    log.setCalories(request.calories());
    log.setProteinG(request.proteinG());
    log.setCarbsG(request.carbsG());
    log.setFatG(request.fatG());
    log.setSource(request.source());
    log.setConfidence(request.confidence());
  }

  private FoodLog resolveOwnedLog(Long id, Long userId) {
    return foodLogRepository
        .findByIdAndUserId(id, userId)
        .orElseThrow(() -> new EntityNotFoundException("Comida no encontrada"));
  }

  private double sum(List<FoodLog> logs, java.util.function.Function<FoodLog, Double> extractor) {
    return logs.stream().map(extractor).filter(v -> v != null).mapToDouble(Double::doubleValue).sum();
  }

  private Double remaining(Double target, double consumed) {
    return target != null ? target - consumed : null;
  }

  private int zero(Integer value) {
    return value != null ? value : 0;
  }
}
