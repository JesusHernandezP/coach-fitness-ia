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

  @Column(name = "protein_g")
  private Double proteinG;

  @Column(name = "carbs_g")
  private Double carbsG;

  @Column(name = "fat_g")
  private Double fatG;

  @Column(name = "calculated_at")
  private Instant calculatedAt;
}
