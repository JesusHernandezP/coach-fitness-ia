package com.fitnesscoach.chat;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingAiActionRepository extends JpaRepository<PendingAiAction, Long> {

  Optional<PendingAiAction> findFirstByUserIdAndConversationIdAndStatusOrderByCreatedAtDesc(
      Long userId, Long conversationId, PendingAiActionStatus status);

  long countByUserIdAndConversationIdAndStatusAndExpiresAtAfter(
      Long userId, Long conversationId, PendingAiActionStatus status, Instant instant);
}
