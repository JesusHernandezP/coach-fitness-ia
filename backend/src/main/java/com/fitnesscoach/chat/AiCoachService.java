package com.fitnesscoach.chat;

import com.fitnesscoach.nutrition.DailyNutritionSummary;
import com.fitnesscoach.nutrition.FoodLogResponse;
import com.fitnesscoach.nutrition.FoodLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiCoachService {

  private static final String SYSTEM_PROMPT =
      "Eres un coach personal con doble rol de nutricionista certificado y entrenador fisico. "
          + "Da respuestas breves, practicas y personalizadas.";

  private final AiContextService aiContextService;
  private final FoodEstimationService foodEstimationService;
  private final PendingAiActionService pendingAiActionService;
  private final FoodLogService foodLogService;
  private final AiMemoryService aiMemoryService;
  private final GroqClient groqClient;

  public String answer(
      Long userId,
      ChatConversation conversation,
      String userText,
      List<Map<String, String>> history,
      boolean hasPendingAction) {
    aiMemoryService.rememberIfUseful(userId, userText);
    AiContextSnapshot context = aiContextService.build(userId, userText);
    String normalized = normalize(userText);

    if (!context.hasProfile()) {
      return "Necesito que completes tu perfil primero para darte calculos fiables y registrar comidas.";
    }

    if (isPendingRejection(normalized) && hasPendingAction) {
      PendingAiAction action = pendingAiActionService.findActivePending(userId, conversation.getId());
      pendingAiActionService.reject(action);
      return "Entendido. No registro esa comida. Si quieres, dame una version corregida y la vuelvo a estimar.";
    }

    if (isPendingConfirmation(normalized) && hasPendingAction) {
      PendingAiAction action = pendingAiActionService.findActivePending(userId, conversation.getId());
      FoodLogResponse saved = pendingAiActionService.confirmFoodLog(action);
      DailyNutritionSummary summary = foodLogService.todaySummary(userId);
      return String.format(
          Locale.US,
          "Listo. He registrado \"%s\" con %.0f kcal. Ahora te quedan %.0f kcal y %.0f g de proteina hoy.",
          saved.description(),
          saved.calories(),
          value(summary.remainingCalories()),
          value(summary.remainingProteinG()));
    }

    if (asksForRemaining(normalized)) {
      DailyNutritionSummary summary = foodLogService.todaySummary(userId);
      return String.format(
          Locale.US,
          "Hoy llevas %.0f kcal, %.0f g de proteina, %.0f g de carbs y %.0f g de grasa. Te quedan %.0f kcal y %.0f g de proteina.",
          summary.consumedCalories(),
          summary.consumedProteinG(),
          summary.consumedCarbsG(),
          summary.consumedFatG(),
          value(summary.remainingCalories()),
          value(summary.remainingProteinG()));
    }

    if (looksLikeMealLog(normalized)) {
      FoodEstimate estimate = foodEstimationService.estimate(userText, context.prompt());
      pendingAiActionService.createFoodLogAction(conversation, estimate);
      return String.format(
          Locale.US,
          "He estimado esta comida: %s, %.0f kcal, %.0f g proteina, %.0f g carbs y %.0f g grasa. Si quieres que la registre, responde \"si, registralo\".",
          estimate.description(),
          estimate.calories(),
          value(estimate.proteinG()),
          value(estimate.carbsG()),
          value(estimate.fatG()));
    }

    return groqClient.complete(buildMessages(context.prompt(), history, userText));
  }

  boolean asksForRemaining(String text) {
    return text.contains("cuanto me falta")
        || text.contains("qué me falta")
        || text.contains("que me falta")
        || text.contains("resta hoy");
  }

  boolean isPendingConfirmation(String text) {
    return text.equals("si")
        || text.equals("sí")
        || text.contains("registralo")
        || text.contains("regístralo")
        || text.contains("confirmo");
  }

  boolean isPendingRejection(String text) {
    return text.contains("descarta") || text.contains("no la registres") || text.contains("olvidalo");
  }

  boolean looksLikeMealLog(String text) {
    return text.contains("he comido")
        || text.contains("desayune")
        || text.contains("desayuné")
        || text.contains("comi ")
        || text.contains("comí ")
        || text.contains("cene")
        || text.contains("cené")
        || text.contains("snack")
        || text.contains("merende")
        || text.contains("merendé");
  }

  private List<Map<String, String>> buildMessages(
      String contextPrompt, List<Map<String, String>> history, String userText) {
    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT + " " + contextPrompt));
    messages.addAll(history);
    messages.add(Map.of("role", "user", "content", userText));
    return messages;
  }

  private String normalize(String text) {
    return text.toLowerCase(Locale.ROOT).trim();
  }

  private double value(Double value) {
    return value != null ? value : 0.0;
  }
}
