package com.fitnesscoach.chat;

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

  private final ChatConversationRepository conversationRepo;
  private final ChatMessageRepository messageRepo;
  private final PendingAiActionRepository pendingAiActionRepository;
  private final AiCoachService aiCoachService;

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

    ChatConversation conversation = resolveConversation(conversationId, userId);

    ChatMessage userMsg =
        messageRepo.save(
            ChatMessage.builder()
                .conversation(conversation)
                .role(MessageRole.user)
                .content(req.content())
                .build());

    boolean hasPendingAction =
        pendingAiActionRepository.countByUserIdAndConversationIdAndStatusAndExpiresAtAfter(
                userId, conversationId, PendingAiActionStatus.pending, Instant.now())
            > 0;

    String reply =
        aiCoachService.answer(
            userId,
            conversation,
            req.content(),
            buildHistory(conversationId, userMsg),
            hasPendingAction);

    ChatMessage assistantMsg =
        messageRepo.save(
            ChatMessage.builder()
                .conversation(conversation)
                .role(MessageRole.assistant)
                .content(reply)
                .build());

    return MessageResponse.from(assistantMsg);
  }

  private List<Map<String, String>> buildHistory(Long conversationId, ChatMessage latestUserMsg) {
    List<Map<String, String>> messages = new ArrayList<>();
    messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
        .filter(m -> !m.getId().equals(latestUserMsg.getId()))
        .forEach(m -> messages.add(Map.of("role", m.getRole().name(), "content", m.getContent())));
    return messages;
  }

  private void enforceRateLimit(Long userId) {
    Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
    long count = messageRepo.countByConversationUserIdAndCreatedAtAfter(userId, oneHourAgo);
    if (count >= RATE_LIMIT) {
      throw new RateLimitException();
    }
  }

  private ChatConversation resolveConversation(Long conversationId, Long userId) {
    return conversationRepo
        .findByIdAndUserId(conversationId, userId)
        .orElseThrow(() -> new EntityNotFoundException("Conversacion no encontrada"));
  }
}
