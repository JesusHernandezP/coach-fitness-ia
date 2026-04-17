package com.fitnesscoach.profile;

import com.fitnesscoach.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metabolic_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetabolicProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  private Integer age;

  @Enumerated(EnumType.STRING)
  private Sex sex;

  private Double heightCm;
  private Double currentWeightKg;

  @Enumerated(EnumType.STRING)
  private ActivityLevel activityLevel;

  private Integer weeklyExerciseDays;
  private String exerciseType;
  private Integer exerciseMinutes;
  private Integer dailySteps;

  @Enumerated(EnumType.STRING)
  private DietType dietType;

  @Enumerated(EnumType.STRING)
  private Goal goal;

  private Instant updatedAt;

  @PrePersist
  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
