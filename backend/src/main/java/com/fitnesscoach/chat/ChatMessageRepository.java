package com.fitnesscoach.chat;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

  long countByConversationUserIdAndCreatedAtAfter(Long userId, Instant since);
}
