package com.fitnesscoach.activity;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Registro diario de actividad fisica")
@SecurityRequirement(name = "bearerAuth")
public class ActivityLogController {

  private final ActivityLogService activityLogService;

  @PostMapping
  @Operation(summary = "Crear o actualizar actividad del dia (upsert por fecha)")
  public ActivityLogResponse upsert(
      @AuthenticationPrincipal User user, @Valid @RequestBody ActivityLogRequest request) {
    return activityLogService.upsert(user, request);
  }

  @GetMapping
  @Operation(summary = "Listar actividades (rango opcional)")
  public List<ActivityLogResponse> list(
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    return activityLogService.list(user.getId(), from, to);
  }

  @GetMapping("/today")
  @Operation(summary = "Actividad de hoy (0s si no hay registro)")
  public ActivityLogResponse today(@AuthenticationPrincipal User user) {
    return activityLogService.getToday(user.getId());
  }
}
