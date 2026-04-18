package com.fitnesscoach.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fitnesscoach.user.User;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

  @Mock ActivityLogRepository activityRepo;

  @InjectMocks ActivityLogService activityLogService;

  private final User user = User.builder().id(1L).email("test@test.com").build();

  @Test
  void upsert_createsNewLog_whenNoneExistsForDate() {
    ActivityLogRequest req = new ActivityLogRequest(LocalDate.of(2025, 4, 17), 9000, 350, "run");
    when(activityRepo.findByUserIdAndDate(1L, req.date())).thenReturn(Optional.empty());
    when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ActivityLogResponse res = activityLogService.upsert(user, req);

    assertThat(res.steps()).isEqualTo(9000);
    assertThat(res.caloriesBurned()).isEqualTo(350);
    verify(activityRepo).save(any(ActivityLog.class));
  }

  @Test
  void upsert_updatesExistingLog_onSameDate() {
    LocalDate today = LocalDate.now();
    ActivityLog existing =
        ActivityLog.builder().id(1L).user(user).date(today).steps(5000).caloriesBurned(200).build();
    ActivityLogRequest req = new ActivityLogRequest(today, 8000, null, null);

    when(activityRepo.findByUserIdAndDate(1L, today)).thenReturn(Optional.of(existing));
    when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ActivityLogResponse res = activityLogService.upsert(user, req);

    assertThat(res.steps()).isEqualTo(8000);
    assertThat(res.caloriesBurned()).isEqualTo(200); // unchanged
  }

  @Test
  void getToday_returnsZeros_whenNoLogExists() {
    when(activityRepo.findByUserIdAndDate(eq(1L), any())).thenReturn(Optional.empty());

    ActivityLogResponse res = activityLogService.getToday(1L);

    assertThat(res.steps()).isEqualTo(0);
    assertThat(res.caloriesBurned()).isEqualTo(0);
    assertThat(res.id()).isNull();
  }
}
