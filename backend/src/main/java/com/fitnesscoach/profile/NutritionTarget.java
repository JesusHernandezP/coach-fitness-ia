package com.fitnesscoach.profile;

import com.fitnesscoach.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nutrition_targets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTarget {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  private Double calories;
  private Double proteinG;
  private Double carbsG;
  private Double fatG;
  private Instant calculatedAt;
}
