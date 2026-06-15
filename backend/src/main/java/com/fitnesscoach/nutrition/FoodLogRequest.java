package com.fitnesscoach.nutrition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record FoodLogRequest(
    @NotNull @Schema(example = "2026-06-15") LocalDate date,
    @NotNull MealType mealType,
    @NotBlank @Schema(example = "Tortilla francesa con pan integral") String description,
    @NotNull @DecimalMin(value = "0.0") @Schema(example = "540") Double calories,
    @DecimalMin(value = "0.0") @Schema(example = "38") Double proteinG,
    @DecimalMin(value = "0.0") @Schema(example = "42") Double carbsG,
    @DecimalMin(value = "0.0") @Schema(example = "21") Double fatG,
    @NotNull FoodLogSource source,
    @DecimalMin(value = "0.0") @Schema(example = "0.92") Double confidence) {}
