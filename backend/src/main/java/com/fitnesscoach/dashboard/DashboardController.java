package com.fitnesscoach.dashboard;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Agregaciones para el panel principal")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/weight-progress")
  @Operation(summary = "Serie temporal de peso (ultimos N dias, default 90)")
  public List<WeightProgressPoint> weightProgress(
      @AuthenticationPrincipal User user, @RequestParam(defaultValue = "90") int days) {
    return dashboardService.weightProgress(user.getId(), days);
  }

  @GetMapping("/weekly-summary")
  @Operation(summary = "Resumen de los ultimos 7 dias")
  public WeeklySummary weeklySummary(@AuthenticationPrincipal User user) {
    return dashboardService.weeklySummary(user.getId());
  }

  @GetMapping("/today")
  @Operation(summary = "Snapshot del dia actual")
  public TodaySnapshot today(@AuthenticationPrincipal User user) {
    return dashboardService.today(user.getId());
  }
}
