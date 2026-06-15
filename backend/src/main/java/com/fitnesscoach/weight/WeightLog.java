package com.fitnesscoach.weight;

import com.fitnesscoach.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weight_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "weight_kg", nullable = false)
  private Double weightKg;

  @Column(name = "logged_at", nullable = false)
  private Instant loggedAt;

  @PrePersist
  void prePersist() {
    if (loggedAt == null) loggedAt = Instant.now();
  }
}
