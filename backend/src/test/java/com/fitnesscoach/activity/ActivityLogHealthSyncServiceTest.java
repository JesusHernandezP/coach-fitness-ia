package com.fitnesscoach.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fitnesscoach.user.User;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityLogHealthSyncServiceTest {

  @Mock ActivityLogRepository activityRepo;

  @InjectMocks ActivityLogService activityLogService;

  private final User user = User.builder().id(1L).email("test@test.com").build();

  @Test
  void upsertHealthSync_createsActivityWhenMissing() {
    LocalDate date = LocalDate.now();
    when(activityRepo.findByUserIdAndDate(1L, date)).thenReturn(Optional.empty());
    when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ActivityLogResponse response =
        activityLogService.upsertHealthSync(
            user, new DailyHealthSyncRequest(date, 8500, 430, "health_connect"));

    assertThat(response.date()).isEqualTo(date);
    assertThat(response.steps()).isEqualTo(8500);
    assertThat(response.caloriesBurned()).isEqualTo(430);
    assertThat(response.source()).isEqualTo("health_connect");
    assertThat(response.syncedAt()).isNotNull();
  }

  @Test
  void upsertHealthSync_updatesSyncedValuesAndKeepsNotes() {
    LocalDate date = LocalDate.now();
    ActivityLog existing =
        ActivityLog.builder()
            .id(10L)
            .user(user)
            .date(date)
            .steps(2000)
            .caloriesBurned(100)
            .notes("manual note")
            .source("manual")
            .build();
    when(activityRepo.findByUserIdAndDate(1L, date)).thenReturn(Optional.of(existing));
    when(activityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ActivityLogResponse response =
        activityLogService.upsertHealthSync(
            user, new DailyHealthSyncRequest(date, 9200, 510, "health_connect"));

    assertThat(response.steps()).isEqualTo(9200);
    assertThat(response.caloriesBurned()).isEqualTo(510);
    assertThat(response.notes()).isEqualTo("manual note");
    assertThat(response.source()).isEqualTo("health_connect");
  }
}
