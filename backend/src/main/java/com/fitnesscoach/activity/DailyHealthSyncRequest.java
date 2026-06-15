package com.fitnesscoach.activity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DailyHealthSyncRequest(
    @NotNull LocalDate date,
    @NotNull @Min(0) Integer steps,
    @NotNull @Min(0) Integer caloriesBurned,
    @NotBlank String source) {}
