package com.fitnesscoach.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotBlank @Size(max = 2000) @Schema(example = "Que desayuno recomiendas?") String content) {}
