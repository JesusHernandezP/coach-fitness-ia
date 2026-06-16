package com.fitnesscoach.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodEstimationService {

  private final GroqClient groqClient;
  private final ObjectMapper objectMapper;

  public FoodEstimate estimate(String userText, String contextPrompt) {
    String prompt =
        """
        Estima una comida a partir del texto del usuario y devuelve SOLO JSON valido.
        Campos: description, mealType, calories, proteinG, carbsG, fatG, rationale.
        mealType debe ser uno de: breakfast, lunch, dinner, snack, other.
        Contexto:
        """
            + contextPrompt
            + "\nTexto del usuario: "
            + userText;

    String raw =
        groqClient.complete(
            List.of(
                Map.of("role", "system", "content", "Eres un estimador nutricional. Respondes solo JSON."),
                Map.of("role", "user", "content", prompt)));
    try {
      return objectMapper.readValue(cleanJson(raw), FoodEstimate.class);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("No se pudo interpretar la estimacion nutricional", ex);
    }
  }

  private String cleanJson(String response) {
    String trimmed = response.trim();
    if (trimmed.startsWith("```")) {
      int firstBreak = trimmed.indexOf('\n');
      int lastFence = trimmed.lastIndexOf("```");
      if (firstBreak > -1 && lastFence > firstBreak) {
        return trimmed.substring(firstBreak + 1, lastFence).trim();
      }
    }
    return trimmed;
  }
}
