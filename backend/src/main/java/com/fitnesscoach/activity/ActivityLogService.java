package com.fitnesscoach.activity;

import com.fitnesscoach.user.User;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

  private final ActivityLogRepository activityRepo;

  @Transactional
  public ActivityLogResponse upsert(User user, ActivityLogRequest req) {
    LocalDate date = req.date() != null ? req.date() : LocalDate.now();

    ActivityLog log =
        activityRepo
            .findByUserIdAndDate(user.getId(), date)
            .orElse(ActivityLog.builder().user(user).date(date).build());

    if (req.steps() != null) log.setSteps(req.steps());
    if (req.caloriesBurned() != null) log.setCaloriesBurned(req.caloriesBurned());
    if (req.notes() != null) log.setNotes(req.notes());

    return ActivityLogResponse.from(activityRepo.save(log));
  }

  public List<ActivityLogResponse> list(Long userId, LocalDate from, LocalDate to) {
    List<ActivityLog> logs =
        (from != null && to != null)
            ? activityRepo.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)
            : activityRepo.findByUserIdOrderByDateAsc(userId);
    return logs.stream().map(ActivityLogResponse::from).toList();
  }

  public ActivityLogResponse getToday(Long userId) {
    return activityRepo
        .findByUserIdAndDate(userId, LocalDate.now())
        .map(ActivityLogResponse::from)
        .orElse(new ActivityLogResponse(null, LocalDate.now(), 0, 0, null));
  }
}
