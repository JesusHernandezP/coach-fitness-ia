package com.fitnesscoach.nutrition;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/food-logs")
@RequiredArgsConstructor
@Tag(name = "Food Logs", description = "Diario nutricional de comidas")
@SecurityRequirement(name = "bearerAuth")
public class FoodLogController {

  private final FoodLogService foodLogService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Registrar una comida")
  public FoodLogResponse create(
      @AuthenticationPrincipal User user, @Valid @RequestBody FoodLogRequest request) {
    return foodLogService.create(user, request);
  }

  @GetMapping
  @Operation(summary = "Listar comidas en rango")
  public List<FoodLogResponse> list(
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    return foodLogService.list(user.getId(), from, to);
  }

  @GetMapping("/today")
  @Operation(summary = "Listar comidas de hoy")
  public List<FoodLogResponse> today(@AuthenticationPrincipal User user) {
    return foodLogService.listToday(user.getId());
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar una comida")
  public FoodLogResponse update(
      @AuthenticationPrincipal User user,
      @PathVariable Long id,
      @Valid @RequestBody FoodLogRequest request) {
    return foodLogService.update(id, user.getId(), request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Eliminar una comida")
  public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
    foodLogService.delete(id, user.getId());
  }
}
