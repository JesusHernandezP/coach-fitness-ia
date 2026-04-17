package com.fitnesscoach.weight;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weights")
@RequiredArgsConstructor
@Tag(name = "Weights", description = "Historial de peso")
@SecurityRequirement(name = "bearerAuth")
public class WeightLogController {

  private final WeightLogService weightLogService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Registrar nuevo peso")
  public WeightLogResponse create(
      @AuthenticationPrincipal User user, @Valid @RequestBody WeightLogRequest request) {
    return weightLogService.create(user, request);
  }

  @GetMapping
  @Operation(summary = "Listar registros de peso (rango opcional)")
  public List<WeightLogResponse> list(
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    return weightLogService.list(user.getId(), from, to);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Eliminar registro de peso")
  public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
    weightLogService.delete(id, user.getId());
  }
}
