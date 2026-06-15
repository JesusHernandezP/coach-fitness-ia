package com.fitnesscoach.chat;

import com.fitnesscoach.nutrition.MealType;

public record FoodEstimate(
    String description,
    MealType mealType,
    Double calories,
    Double proteinG,
    Double carbsG,
    Double fatG,
    String rationale) {}
