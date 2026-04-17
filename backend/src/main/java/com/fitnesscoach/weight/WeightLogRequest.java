package com.fitnesscoach.weight;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record WeightLogRequest(
    @NotNull @DecimalMin("20") @DecimalMax("500") @Schema(example = "78.5") Double weightKg,
    @Schema(description = "ISO-8601, defaults to now if omitted") Instant loggedAt) {}
