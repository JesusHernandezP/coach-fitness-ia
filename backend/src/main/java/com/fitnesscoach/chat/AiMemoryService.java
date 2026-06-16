package com.fitnesscoach.chat;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiMemoryService {

  private final AiMemoryRepository aiMemoryRepository;
  private final EmbeddingClient embeddingClient;

  public void rememberIfUseful(Long userId, String userText) {
    MemoryCandidate candidate = detectCandidate(userText);
    if (candidate == null || aiMemoryRepository.exists(userId, candidate.type(), candidate.content())) {
      return;
    }

    aiMemoryRepository.save(
        userId,
        candidate.type(),
        candidate.content(),
        formatVector(embeddingClient.embed(candidate.content())),
        candidate.importance());
  }

  public List<AiMemory> listForUser(Long userId) {
    return aiMemoryRepository.findForUser(userId);
  }

  MemoryCandidate detectCandidate(String userText) {
    String normalized = userText == null ? "" : userText.toLowerCase(Locale.ROOT).trim();
    if (normalized.isBlank()) {
      return null;
    }

    if (normalized.contains("soy alergico")
        || normalized.contains("soy alérgico")
        || normalized.contains("no puedo comer")
        || normalized.contains("intoler")
        || normalized.contains("evito el gluten")
        || normalized.contains("evito lactosa")) {
      return new MemoryCandidate(AiMemoryType.restriction, userText.trim(), 5);
    }

    if (normalized.contains("prefiero")
        || normalized.contains("me gusta entrenar")
        || normalized.contains("suelo entrenar")
        || normalized.contains("me sienta mejor")) {
      return new MemoryCandidate(AiMemoryType.preference, userText.trim(), 3);
    }

    if (normalized.contains("me duele")
        || normalized.contains("lesion")
        || normalized.contains("lesión")
        || normalized.contains("no puedo hacer sentadillas")) {
      return new MemoryCandidate(AiMemoryType.training_note, userText.trim(), 4);
    }

    if (normalized.contains("desayuno siempre")
        || normalized.contains("ceno siempre")
        || normalized.contains("no desayuno")
        || normalized.contains("me cuesta llegar a la proteina")
        || normalized.contains("me cuesta llegar a la proteína")) {
      return new MemoryCandidate(AiMemoryType.nutrition_note, userText.trim(), 3);
    }

    return null;
  }

  String formatVector(List<Double> values) {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        builder.append(',');
      }
      builder.append(values.get(i));
    }
    builder.append(']');
    return builder.toString();
  }

  record MemoryCandidate(AiMemoryType type, String content, int importance) {}
}
