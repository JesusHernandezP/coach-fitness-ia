package com.fitnesscoach.nutrition;

import com.fitnesscoach.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private LocalDate date;

  @Enumerated(EnumType.STRING)
  @Column(name = "meal_type", nullable = false, length = 50)
  private MealType mealType;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Double calories;

  @Column(name = "protein_g")
  private Double proteinG;

  @Column(name = "carbs_g")
  private Double carbsG;

  @Column(name = "fat_g")
  private Double fatG;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private FoodLogSource source;

  private Double confidence;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
