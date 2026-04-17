package com.fitnesscoach.activity;

import com.fitnesscoach.user.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "activity_logs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private LocalDate date;

  private Integer steps;
  private Integer caloriesBurned;
  private String notes;
}
