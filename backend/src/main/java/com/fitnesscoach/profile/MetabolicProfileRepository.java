package com.fitnesscoach.profile;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetabolicProfileRepository extends JpaRepository<MetabolicProfile, Long> {
  Optional<MetabolicProfile> findByUserId(Long userId);
}
