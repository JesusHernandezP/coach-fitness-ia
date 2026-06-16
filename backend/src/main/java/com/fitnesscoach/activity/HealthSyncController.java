package com.fitnesscoach.activity;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health-sync")
@RequiredArgsConstructor
@Tag(name = "Health Sync", description = "Sincronizacion de actividad desde Health Connect")
@SecurityRequirement(name = "bearerAuth")
public class HealthSyncController {

  private final ActivityLogService activityLogService;

  @PostMapping("/daily-activity")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Sincroniza pasos y calorias activas del dia")
  public ActivityLogResponse syncDailyActivity(
      @AuthenticationPrincipal User user, @Valid @RequestBody DailyHealthSyncRequest request) {
    return activityLogService.upsertHealthSync(user, request);
  }
}
