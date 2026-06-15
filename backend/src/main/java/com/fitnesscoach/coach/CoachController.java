package com.fitnesscoach.coach;

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
@RequestMapping("/api/v1/coach")
@RequiredArgsConstructor
@Tag(name = "Coach", description = "Funciones IA orientadas a coaching personalizado")
@SecurityRequirement(name = "bearerAuth")
public class CoachController {

  private final WeeklyReviewService weeklyReviewService;

  @GetMapping("/weekly-review")
  @Operation(summary = "Genera una revision semanal personalizada con IA")
  public WeeklyReviewResponse weeklyReview(@AuthenticationPrincipal User user) {
    return weeklyReviewService.generate(user.getId());
  }
}
