package com.fitnesscoach.profile;

import com.fitnesscoach.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Perfil metabolico y objetivos nutricionales")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

  private final ProfileService profileService;

  @GetMapping("/me")
  @Operation(summary = "Obtener perfil del usuario autenticado")
  public ProfileResponse getProfile(@AuthenticationPrincipal User user) {
    return profileService.getProfile(user.getId());
  }

  @PutMapping("/me")
  @Operation(summary = "Guardar perfil y recalcular macros")
  public ProfileResponse saveProfile(
      @AuthenticationPrincipal User user, @Valid @RequestBody ProfileRequest request) {
    return profileService.saveProfile(user.getId(), request);
  }

  @GetMapping("/targets")
  @Operation(summary = "Obtener ultimos macros calculados")
  public NutritionTargetResponse getTargets(@AuthenticationPrincipal User user) {
    return profileService.getTargets(user.getId());
  }
}
