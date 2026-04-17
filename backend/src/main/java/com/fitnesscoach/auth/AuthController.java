package com.fitnesscoach.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro e inicio de sesion")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Registrar nuevo usuario")
  public AuthResponse register(@Valid @RequestBody AuthRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  @Operation(summary = "Iniciar sesion y obtener JWT")
  public AuthResponse login(@Valid @RequestBody AuthRequest request) {
    return authService.login(request);
  }
}
