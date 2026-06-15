package com.fitnesscoach.chat;

import com.fitnesscoach.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pending_ai_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingAiAction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  private ChatConversation conversation;

  @Column(name = "action_type", nullable = false, length = 80)
  private String actionType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private PendingAiActionStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
  }
}
