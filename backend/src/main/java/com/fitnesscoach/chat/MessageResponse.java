package com.fitnesscoach.chat;

import java.time.Instant;

public record MessageResponse(Long id, MessageRole role, String content, Instant createdAt) {
  static MessageResponse from(ChatMessage m) {
    return new MessageResponse(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt());
  }
}
