package com.fitnesscoach.chat;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat con IA (nutricionista + entrenador)")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

  private final ChatService chatService;

  @GetMapping("/conversations")
  @Operation(summary = "Listar conversaciones del usuario")
  public List<ConversationResponse> listConversations(@AuthenticationPrincipal User user) {
    return chatService.listConversations(user.getId());
  }

  @PostMapping("/conversations")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Crear nueva conversacion")
  public ConversationResponse createConversation(
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) String title) {
    return chatService.createConversation(user, title);
  }

  @GetMapping("/conversations/{id}/messages")
  @Operation(summary = "Listar mensajes de una conversacion")
  public List<MessageResponse> listMessages(
      @AuthenticationPrincipal User user, @PathVariable Long id) {
    return chatService.listMessages(id, user.getId());
  }

  @PostMapping("/conversations/{id}/messages")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Enviar mensaje y recibir respuesta del coach IA")
  public MessageResponse sendMessage(
      @AuthenticationPrincipal User user,
      @PathVariable Long id,
      @Valid @RequestBody ChatRequest request) {
    return chatService.sendMessage(id, user.getId(), request);
  }
}
