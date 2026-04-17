package com.fitnesscoach.activity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

  Optional<ActivityLog> findByUserIdAndDate(Long userId, LocalDate date);

  List<ActivityLog> findByUserIdAndDateBetweenOrderByDateAsc(
      Long userId, LocalDate from, LocalDate to);

  List<ActivityLog> findByUserIdOrderByDateAsc(Long userId);
}
