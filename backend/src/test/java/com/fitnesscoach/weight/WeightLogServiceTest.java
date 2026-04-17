package com.fitnesscoach.weight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fitnesscoach.profile.MetabolicProfile;
import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.ProfileService;
import com.fitnesscoach.user.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeightLogServiceTest {

  @Mock WeightLogRepository weightLogRepo;
  @Mock MetabolicProfileRepository profileRepo;
  @Mock ProfileService profileService;

  @InjectMocks WeightLogService weightLogService;

  private final User user = User.builder().id(1L).email("test@test.com").build();

  @Test
  void create_savesLogAndUpdatesProfile() {
    WeightLogRequest req = new WeightLogRequest(79.0, null);
    MetabolicProfile profile = MetabolicProfile.builder().currentWeightKg(80.0).build();

    when(weightLogRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(profileRepo.findByUserId(1L)).thenReturn(Optional.of(profile));
    when(profileRepo.save(any())).thenReturn(profile);

    WeightLogResponse response = weightLogService.create(user, req);

    assertThat(response.weightKg()).isEqualTo(79.0);
    assertThat(profile.getCurrentWeightKg()).isEqualTo(79.0);
    verify(profileService).recalculateTargets(eq(user), eq(profile));
  }

  @Test
  void create_noProfileExists_stillSavesLog() {
    WeightLogRequest req = new WeightLogRequest(75.0, null);
    when(weightLogRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(profileRepo.findByUserId(1L)).thenReturn(Optional.empty());

    WeightLogResponse response = weightLogService.create(user, req);

    assertThat(response.weightKg()).isEqualTo(75.0);
    verifyNoInteractions(profileService);
  }

  @Test
  void delete_throwsNotFound_whenLogBelongsToOtherUser() {
    when(weightLogRepo.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> weightLogService.delete(99L, 1L))
        .isInstanceOf(EntityNotFoundException.class);
  }
}
