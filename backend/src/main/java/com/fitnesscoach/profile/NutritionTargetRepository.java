package com.fitnesscoach.profile;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionTargetRepository extends JpaRepository<NutritionTarget, Long> {
  Optional<NutritionTarget> findByUserId(Long userId);
}
