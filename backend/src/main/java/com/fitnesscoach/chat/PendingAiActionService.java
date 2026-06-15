package com.fitnesscoach.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnesscoach.nutrition.FoodLogRequest;
import com.fitnesscoach.nutrition.FoodLogResponse;
import com.fitnesscoach.nutrition.FoodLogService;
import com.fitnesscoach.nutrition.FoodLogSource;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PendingAiActionService {

  private static final String CREATE_FOOD_LOG = "create_food_log";

  private final PendingAiActionRepository pendingAiActionRepository;
  private final FoodLogService foodLogService;
  private final ObjectMapper objectMapper;

  @Transactional
  public PendingAiAction createFoodLogAction(ChatConversation conversation, FoodEstimate estimate) {
    return pendingAiActionRepository.save(
        PendingAiAction.builder()
            .user(conversation.getUser())
            .conversation(conversation)
            .actionType(CREATE_FOOD_LOG)
            .payload(write(estimate))
            .status(PendingAiActionStatus.pending)
            .expiresAt(Instant.now().plusSeconds(30 * 60))
            .build());
  }

  public PendingAiAction findActivePending(Long userId, Long conversationId) {
    PendingAiAction action =
        pendingAiActionRepository
            .findFirstByUserIdAndConversationIdAndStatusOrderByCreatedAtDesc(
                userId, conversationId, PendingAiActionStatus.pending)
            .orElseThrow(() -> new EntityNotFoundException("No hay acciones pendientes"));
    if (action.getExpiresAt().isBefore(Instant.now())) {
      action.setStatus(PendingAiActionStatus.expired);
      pendingAiActionRepository.save(action);
      throw new IllegalStateException("La accion pendiente ha expirado");
    }
    return action;
  }

  @Transactional
  public FoodLogResponse confirmFoodLog(PendingAiAction action) {
    FoodEstimate estimate = read(action.getPayload());
    action.setStatus(PendingAiActionStatus.confirmed);
    pendingAiActionRepository.save(action);
    return foodLogService.create(
        action.getUser(),
        new FoodLogRequest(
            LocalDate.now(),
            estimate.mealType(),
            estimate.description(),
            estimate.calories(),
            estimate.proteinG(),
            estimate.carbsG(),
            estimate.fatG(),
            FoodLogSource.ai_estimate,
            0.8));
  }

  @Transactional
  public void reject(PendingAiAction action) {
    action.setStatus(PendingAiActionStatus.rejected);
    pendingAiActionRepository.save(action);
  }

  private String write(FoodEstimate estimate) {
    try {
      return objectMapper.writeValueAsString(estimate);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("No se pudo serializar la accion pendiente", ex);
    }
  }

  private FoodEstimate read(String payload) {
    try {
      return objectMapper.readValue(payload, FoodEstimate.class);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("No se pudo leer la accion pendiente", ex);
    }
  }
}
