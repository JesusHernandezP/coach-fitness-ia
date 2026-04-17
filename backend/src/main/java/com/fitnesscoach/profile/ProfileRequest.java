package com.fitnesscoach.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record ProfileRequest(
    @NotNull @Min(10) @Max(120) @Schema(example = "30") Integer age,
    @NotNull Sex sex,
    @NotNull @DecimalMin("100") @DecimalMax("250") @Schema(example = "170") Double heightCm,
    @NotNull @DecimalMin("30") @DecimalMax("300") @Schema(example = "80") Double currentWeightKg,
    @NotNull ActivityLevel activityLevel,
    @Min(0) @Max(7) @Schema(example = "3") Integer weeklyExerciseDays,
    @Schema(example = "gym") String exerciseType,
    @Min(0) @Schema(example = "60") Integer exerciseMinutes,
    @Min(0) @Schema(example = "8000") Integer dailySteps,
    @NotNull DietType dietType,
    @NotNull Goal goal) {}
