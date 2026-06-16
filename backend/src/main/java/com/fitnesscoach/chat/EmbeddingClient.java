package com.fitnesscoach.chat;

import java.util.List;

public interface EmbeddingClient {
  List<Double> embed(String text);
}
