package com.fitnesscoach.chat;

import java.time.Instant;

public record AiMemory(
    Long id,
    Long userId,
    AiMemoryType type,
    String content,
    String embedding,
    int importance,
    Instant createdAt) {}
