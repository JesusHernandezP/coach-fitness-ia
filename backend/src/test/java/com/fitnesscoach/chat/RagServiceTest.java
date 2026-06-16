package com.fitnesscoach.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

  @Mock AiMemoryService aiMemoryService;
  @Mock EmbeddingClient embeddingClient;

  @InjectMocks RagService ragService;

  @Test
  void retrievesOnlyRelevantMemoriesForUser() {
    when(embeddingClient.embed("quiero desayunos sin lactosa")).thenReturn(List.of(1.0, 0.0));
    when(aiMemoryService.listForUser(1L))
        .thenReturn(
            List.of(
                new AiMemory(
                    1L, 1L, AiMemoryType.restriction, "Sin lactosa", "[1.0,0.0]", 5, Instant.now()),
                new AiMemory(
                    2L,
                    1L,
                    AiMemoryType.preference,
                    "Me gusta correr",
                    "[0.0,1.0]",
                    3,
                    Instant.now())));

    List<AiMemory> memories = ragService.retrieveRelevant(1L, "quiero desayunos sin lactosa", 1);

    assertThat(memories).hasSize(1);
    assertThat(memories.get(0).content()).isEqualTo("Sin lactosa");
  }

  @Test
  void buildsEmptyContextWhenNoMemoriesExist() {
    when(embeddingClient.embed("hola")).thenReturn(List.of(1.0, 0.0));
    when(aiMemoryService.listForUser(1L)).thenReturn(List.of());

    assertThat(ragService.buildContext(1L, "hola")).isEmpty();
  }
}
