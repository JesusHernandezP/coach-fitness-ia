package com.fitnesscoach.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitnesscoach.nutrition.DailyNutritionSummary;
import com.fitnesscoach.nutrition.FoodLogResponse;
import com.fitnesscoach.nutrition.FoodLogService;
import com.fitnesscoach.nutrition.FoodLogSource;
import com.fitnesscoach.nutrition.MealType;
import com.fitnesscoach.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiCoachServiceTest {

  @Mock AiContextService aiContextService;
  @Mock FoodEstimationService foodEstimationService;
  @Mock PendingAiActionService pendingAiActionService;
  @Mock FoodLogService foodLogService;
  @Mock AiMemoryService aiMemoryService;
  @Mock GroqClient groqClient;

  @InjectMocks AiCoachService aiCoachService;

  private final User user = User.builder().id(1L).email("a@b.com").build();
  private final ChatConversation conversation =
      ChatConversation.builder().id(7L).user(user).title("Coach").build();

  @Test
  void answersRemainingUsingRealDailySummary() {
    when(aiContextService.build(1L, "cuanto me falta hoy")).thenReturn(new AiContextSnapshot("ctx", true));
    when(foodLogService.todaySummary(1L))
        .thenReturn(
            new DailyNutritionSummary(
                LocalDate.now(), 2360.0, 1280.0, 1080.0, 160.0, 92.0, 68.0, 286.0, 140.0,
                146.0, 64.0, 41.0, 23.0, 430, 850.0));

    String response =
        aiCoachService.answer(1L, conversation, "cuanto me falta hoy", List.of(), false);

    assertThat(response).contains("1080").contains("68");
  }

  @Test
  void createsPendingActionForMealEstimate() {
    when(aiContextService.build(1L, "he comido arroz con pollo")).thenReturn(new AiContextSnapshot("ctx", true));
    when(foodEstimationService.estimate(any(), any()))
        .thenReturn(new FoodEstimate("Arroz con pollo", MealType.lunch, 720.0, 42.0, 80.0, 18.0, ""));

    String response =
        aiCoachService.answer(1L, conversation, "he comido arroz con pollo", List.of(), false);

    assertThat(response).contains("si, registralo").contains("720");
    verify(pendingAiActionService).createFoodLogAction(any(), any());
  }

  @Test
  void confirmationCreatesFoodLog() {
    PendingAiAction action =
        PendingAiAction.builder()
            .id(12L)
            .conversation(conversation)
            .user(user)
            .status(PendingAiActionStatus.pending)
            .expiresAt(Instant.now().plusSeconds(120))
            .build();
    when(aiContextService.build(1L, "si, registralo")).thenReturn(new AiContextSnapshot("ctx", true));
    when(pendingAiActionService.findActivePending(1L, 7L)).thenReturn(action);
    when(pendingAiActionService.confirmFoodLog(action))
        .thenReturn(
            new FoodLogResponse(
                1L, LocalDate.now(), MealType.lunch, "Arroz con pollo", 720.0, 42.0, 80.0, 18.0,
                FoodLogSource.ai_estimate, 0.8, Instant.now(), null));
    when(foodLogService.todaySummary(1L))
        .thenReturn(
            new DailyNutritionSummary(
                LocalDate.now(), 2360.0, 1280.0, 1080.0, 160.0, 92.0, 68.0, 286.0, 140.0,
                146.0, 64.0, 41.0, 23.0, 430, 850.0));

    String response = aiCoachService.answer(1L, conversation, "si, registralo", List.of(), true);

    assertThat(response).contains("He registrado").contains("1080");
  }

  @Test
  void fallsBackToGroqForRegularQuestions() {
    when(aiContextService.build(1L, "dame una cena ligera")).thenReturn(new AiContextSnapshot("ctx", true));
    when(groqClient.complete(any())).thenReturn("Respuesta generica");

    String response = aiCoachService.answer(1L, conversation, "dame una cena ligera", List.of(), false);

    assertThat(response).isEqualTo("Respuesta generica");
  }
}
