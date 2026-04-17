package com.fitnesscoach.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fitnesscoach.profile.MetabolicProfileRepository;
import com.fitnesscoach.user.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock ChatConversationRepository conversationRepo;
  @Mock ChatMessageRepository messageRepo;
  @Mock MetabolicProfileRepository profileRepo;
  @Mock GroqClient groqClient;

  @InjectMocks ChatService chatService;

  private final User user = User.builder().id(1L).email("test@test.com").build();

  @Test
  void createConversation_usesDefaultTitle_whenNullProvided() {
    when(conversationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ConversationResponse res = chatService.createConversation(user, null);

    assertThat(res.title()).isEqualTo("Nueva conversacion");
  }

  @Test
  void sendMessage_throwsRateLimit_whenLimitExceeded() {
    when(messageRepo.countByConversationUserIdAndCreatedAtAfter(eq(1L), any(Instant.class)))
        .thenReturn(20L);

    assertThatThrownBy(() -> chatService.sendMessage(1L, 1L, new ChatRequest("hola")))
        .isInstanceOf(RateLimitException.class);
  }

  @Test
  void sendMessage_returnsAssistantReply_underRateLimit() {
    ChatConversation conv =
        ChatConversation.builder().id(1L).user(user).title("Test").build();
    ChatMessage userMsg =
        ChatMessage.builder().id(10L).conversation(conv).role(MessageRole.user)
            .content("hola").createdAt(Instant.now()).build();
    ChatMessage assistantMsg =
        ChatMessage.builder().id(11L).conversation(conv).role(MessageRole.assistant)
            .content("Hola! Como puedo ayudarte?").createdAt(Instant.now()).build();

    when(messageRepo.countByConversationUserIdAndCreatedAtAfter(eq(1L), any())).thenReturn(0L);
    when(conversationRepo.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(conv));
    when(profileRepo.findByUserId(1L)).thenReturn(Optional.empty());
    when(messageRepo.save(any())).thenReturn(userMsg).thenReturn(assistantMsg);
    when(messageRepo.findByConversationIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
    when(groqClient.complete(any())).thenReturn("Hola! Como puedo ayudarte?");

    MessageResponse res = chatService.sendMessage(1L, 1L, new ChatRequest("hola"));

    assertThat(res.role()).isEqualTo(MessageRole.assistant);
    assertThat(res.content()).isEqualTo("Hola! Como puedo ayudarte?");
  }

  @Test
  void listMessages_throwsNotFound_whenConversationBelongsToOtherUser() {
    when(conversationRepo.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> chatService.listMessages(99L, 1L))
        .isInstanceOf(EntityNotFoundException.class);
  }
}
