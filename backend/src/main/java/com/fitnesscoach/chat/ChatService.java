package com.fitnesscoach.chat;

import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.user.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private static final int RATE_LIMIT = 20;
  private static final String SYSTEM_PROMPT =
      "Eres un coach personal con doble rol de nutricionista certificado y entrenador fisico. "
          + "Da respuestas breves, practicas y personalizadas. "
          + "Usa el perfil del usuario cuando este disponible para contextualizar tus consejos.";

  private final ChatConversationRepository conversationRepo;
  private final ChatMessageRepository messageRepo;
  private final MetabolicProfileRepository profileRepo;
  private final GroqClient groqClient;

  public List<ConversationResponse> listConversations(Long userId) {
    return conversationRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(ConversationResponse::from)
        .toList();
  }

  @Transactional
  public ConversationResponse createConversation(User user, String title) {
    ChatConversation conv =
        ChatConversation.builder()
            .user(user)
            .title(title != null ? title : "Nueva conversacion")
            .build();
    return ConversationResponse.from(conversationRepo.save(conv));
  }

  public List<MessageResponse> listMessages(Long conversationId, Long userId) {
    resolveConversation(conversationId, userId);
    return messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
        .map(MessageResponse::from)
        .toList();
  }

  @Transactional
  public MessageResponse sendMessage(Long conversationId, Long userId, ChatRequest req) {
    enforceRateLimit(userId);

    ChatConversation conv = resolveConversation(conversationId, userId);

    ChatMessage userMsg =
        messageRepo.save(
            ChatMessage.builder()
                .conversation(conv)
                .role(MessageRole.user)
                .content(req.content())
                .build());

    List<Map<String, String>> groqMessages = buildGroqPayload(conversationId, userId, userMsg);
    String reply = groqClient.complete(groqMessages);

    ChatMessage assistantMsg =
        messageRepo.save(
            ChatMessage.builder()
                .conversation(conv)
                .role(MessageRole.assistant)
                .content(reply)
                .build());

    return MessageResponse.from(assistantMsg);
  }

  private List<Map<String, String>> buildGroqPayload(
      Long conversationId, Long userId, ChatMessage latestUserMsg) {

    String systemContent = buildSystemPrompt(userId);
    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", systemContent));

    // Include prior history (excluding the message just saved)
    messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
        .filter(m -> !m.getId().equals(latestUserMsg.getId()))
        .forEach(m -> messages.add(Map.of("role", m.getRole().name(), "content", m.getContent())));

    messages.add(Map.of("role", "user", "content", latestUserMsg.getContent()));
    return messages;
  }

  private String buildSystemPrompt(Long userId) {
    return profileRepo
        .findByUserId(userId)
        .map(
            p ->
                SYSTEM_PROMPT
                    + String.format(
                        " Perfil del usuario: %s, %d anos, %.0f cm, %.1f kg, objetivo: %s, dieta: %s.",
                        p.getSex(), p.getAge(), p.getHeightCm(),
                        p.getCurrentWeightKg(), p.getGoal(), p.getDietType()))
        .orElse(SYSTEM_PROMPT);
  }

  private void enforceRateLimit(Long userId) {
    Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
    long count = messageRepo.countByConversationUserIdAndCreatedAtAfter(userId, oneHourAgo);
    if (count >= RATE_LIMIT) throw new RateLimitException();
  }

  private ChatConversation resolveConversation(Long conversationId, Long userId) {
    return conversationRepo
        .findByIdAndUserId(conversationId, userId)
        .orElseThrow(() -> new EntityNotFoundException("Conversacion no encontrada"));
  }
}
