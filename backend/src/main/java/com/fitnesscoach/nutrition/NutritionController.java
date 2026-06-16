package com.fitnesscoach.nutrition;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
@Tag(name = "Nutrition", description = "Resumennes nutricionales del diario")
@SecurityRequirement(name = "bearerAuth")
public class NutritionController {

  private final FoodLogService foodLogService;

  @GetMapping("/today")
  @Operation(summary = "Resumen nutricional del dia actual")
  public DailyNutritionSummary today(@AuthenticationPrincipal User user) {
    return foodLogService.todaySummary(user.getId());
  }

  @GetMapping("/weekly-summary")
  @Operation(summary = "Resumen nutricional de los ultimos 7 dias")
  public NutritionWeeklySummaryResponse weeklySummary(@AuthenticationPrincipal User user) {
    return foodLogService.weeklySummary(user.getId());
  }
}
