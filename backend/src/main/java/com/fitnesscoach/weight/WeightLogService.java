package com.fitnesscoach.weight;

import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.profile.ProfileService;
import com.fitnesscoach.user.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeightLogService {

  private final WeightLogRepository weightLogRepo;
  private final MetabolicProfileRepository profileRepo;
  private final ProfileService profileService;

  @Transactional
  public WeightLogResponse create(User user, WeightLogRequest req) {
    WeightLog log =
        WeightLog.builder()
            .user(user)
            .weightKg(req.weightKg())
            .loggedAt(req.loggedAt() != null ? req.loggedAt() : Instant.now())
            .build();
    weightLogRepo.save(log);

    // Update current weight on profile and recalculate targets
    profileRepo
        .findByUserId(user.getId())
        .ifPresent(
            profile -> {
              profile.setCurrentWeightKg(req.weightKg());
              profileRepo.save(profile);
              profileService.recalculateTargets(user, profile);
            });

    return WeightLogResponse.from(log);
  }

  public List<WeightLogResponse> list(Long userId, Instant from, Instant to) {
    List<WeightLog> logs =
        (from != null && to != null)
            ? weightLogRepo.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, from, to)
            : weightLogRepo.findByUserIdOrderByLoggedAtAsc(userId);
    return logs.stream().map(WeightLogResponse::from).toList();
  }

  @Transactional
  public void delete(Long id, Long userId) {
    WeightLog log =
        weightLogRepo
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado"));
    weightLogRepo.delete(log);
  }
}
