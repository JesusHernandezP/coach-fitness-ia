package com.fitnesscoach.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class HashingEmbeddingClient implements EmbeddingClient {

  static final int DIMENSIONS = 1536;

  @Override
  public List<Double> embed(String text) {
    double[] vector = new double[DIMENSIONS];
    String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    if (normalized.isBlank()) {
      return toList(vector);
    }

    for (String token : normalized.split("\\s+")) {
      int index = Math.floorMod(token.hashCode(), DIMENSIONS);
      vector[index] += 1.0;
    }

    normalize(vector);
    return toList(vector);
  }

  private void normalize(double[] vector) {
    double magnitude = Math.sqrt(Arrays.stream(vector).map(v -> v * v).sum());
    if (magnitude == 0) {
      return;
    }
    for (int i = 0; i < vector.length; i++) {
      vector[i] = vector[i] / magnitude;
    }
  }

  private List<Double> toList(double[] vector) {
    List<Double> values = new ArrayList<>(vector.length);
    for (double value : vector) {
      values.add(value);
    }
    return values;
  }
}
