package com.fitnesscoach.nutrition;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

  List<FoodLog> findByUserIdAndDateBetweenOrderByDateAscCreatedAtAsc(
      Long userId, LocalDate from, LocalDate to);

  List<FoodLog> findByUserIdAndDateOrderByCreatedAtAsc(Long userId, LocalDate date);

  Optional<FoodLog> findByIdAndUserId(Long id, Long userId);
}
