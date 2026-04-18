package com.fitnesscoach.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
    @NotBlank @Email @Schema(example = "usuario@ejemplo.com") String email,
    @NotBlank
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Schema(example = "secreto123")
        String password) {}
