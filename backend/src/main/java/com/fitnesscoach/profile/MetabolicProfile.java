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

  @Column(name = "height_cm")
  private Double heightCm;

  @Column(name = "current_weight_kg")
  private Double currentWeightKg;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_level")
  private ActivityLevel activityLevel;

  @Column(name = "weekly_exercise_days")
  private Integer weeklyExerciseDays;

  @Column(name = "exercise_type")
  private String exerciseType;

  @Column(name = "exercise_minutes")
  private Integer exerciseMinutes;

  @Column(name = "daily_steps")
  private Integer dailySteps;

  @Enumerated(EnumType.STRING)
  @Column(name = "diet_type")
  private DietType dietType;

  @Enumerated(EnumType.STRING)
  private Goal goal;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
