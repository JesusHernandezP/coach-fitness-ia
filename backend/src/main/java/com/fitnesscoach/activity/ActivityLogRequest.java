package com.fitnesscoach.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public record ActivityLogRequest(
    @Schema(description = "Defaults to today if omitted") LocalDate date,
    @Min(0) @Schema(example = "8500") Integer steps,
    @Min(0) @Schema(example = "320") Integer caloriesBurned,
    @Schema(example = "30 min cardio") String notes) {}
