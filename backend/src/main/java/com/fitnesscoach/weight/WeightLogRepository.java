package com.fitnesscoach.weight;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {

  List<WeightLog> findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
      Long userId, Instant from, Instant to);

  List<WeightLog> findByUserIdOrderByLoggedAtAsc(Long userId);

  Optional<WeightLog> findByIdAndUserId(Long id, Long userId);

  @Query("SELECT w FROM WeightLog w WHERE w.user.id = :userId ORDER BY w.loggedAt DESC LIMIT 1")
  Optional<WeightLog> findLatestByUserId(Long userId);
}
