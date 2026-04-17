package com.fitnesscoach.chat;

import java.time.Instant;

public record ConversationResponse(Long id, String title, Instant createdAt) {
  static ConversationResponse from(ChatConversation c) {
    return new ConversationResponse(c.getId(), c.getTitle(), c.getCreatedAt());
  }
}
