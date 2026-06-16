package com.fitnesscoach.chat;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RagService {

  private final AiMemoryService aiMemoryService;
  private final EmbeddingClient embeddingClient;

  public List<AiMemory> retrieveRelevant(Long userId, String query, int limit) {
    List<Double> queryEmbedding = embeddingClient.embed(query);
    return aiMemoryService.listForUser(userId).stream()
        .sorted(
            Comparator.<AiMemory>comparingDouble(
                    memory -> -cosineSimilarity(queryEmbedding, parseVector(memory.embedding())))
                .thenComparing(Comparator.comparingInt(AiMemory::importance).reversed()))
        .limit(limit)
        .toList();
  }

  public String buildContext(Long userId, String query) {
    List<AiMemory> memories =
        retrieveRelevant(userId, query, 3).stream()
            .filter(
                memory ->
                    cosineSimilarity(embeddingClient.embed(query), parseVector(memory.embedding()))
                        > 0.12)
            .toList();
    if (memories.isEmpty()) {
      return "";
    }

    StringBuilder context = new StringBuilder("Memorias relevantes: ");
    for (AiMemory memory : memories) {
      context
          .append('[')
          .append(memory.type().name().toLowerCase(Locale.ROOT))
          .append("] ")
          .append(memory.content())
          .append(". ");
    }
    return context.toString().trim();
  }

  List<Double> parseVector(String value) {
    String cleaned = value.replace("[", "").replace("]", "").trim();
    if (cleaned.isBlank()) {
      return List.of();
    }
    return java.util.Arrays.stream(cleaned.split(","))
        .map(String::trim)
        .map(Double::parseDouble)
        .toList();
  }

  double cosineSimilarity(List<Double> left, List<Double> right) {
    if (left.isEmpty() || right.isEmpty() || left.size() != right.size()) {
      return 0;
    }
    double dot = 0;
    double leftNorm = 0;
    double rightNorm = 0;
    for (int i = 0; i < left.size(); i++) {
      dot += left.get(i) * right.get(i);
      leftNorm += left.get(i) * left.get(i);
      rightNorm += right.get(i) * right.get(i);
    }
    if (leftNorm == 0 || rightNorm == 0) {
      return 0;
    }
    return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
  }
}
