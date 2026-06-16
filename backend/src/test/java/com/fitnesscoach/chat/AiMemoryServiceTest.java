package com.fitnesscoach.chat;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiMemoryServiceTest {

  @Mock AiMemoryRepository aiMemoryRepository;
  @Mock EmbeddingClient embeddingClient;

  @InjectMocks AiMemoryService aiMemoryService;

  @Test
  void storesRestrictionMemoryWhenUseful() {
    when(aiMemoryRepository.exists(1L, AiMemoryType.restriction, "Soy alergico a los frutos secos"))
        .thenReturn(false);
    when(embeddingClient.embed("Soy alergico a los frutos secos")).thenReturn(List.of(1.0, 0.0));

    aiMemoryService.rememberIfUseful(1L, "Soy alergico a los frutos secos");

    verify(aiMemoryRepository)
        .save(
            eq(1L),
            eq(AiMemoryType.restriction),
            eq("Soy alergico a los frutos secos"),
            eq("[1.0,0.0]"),
            anyInt());
  }

  @Test
  void ignoresMessagesWithoutUsefulMemorySignal() {
    aiMemoryService.rememberIfUseful(1L, "hola, que tal");

    verify(aiMemoryRepository, never())
        .save(eq(1L), eq(AiMemoryType.preference), eq("hola, que tal"), eq(""), anyInt());
  }
}
