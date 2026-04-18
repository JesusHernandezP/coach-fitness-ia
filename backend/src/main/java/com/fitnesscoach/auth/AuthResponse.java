package com.fitnesscoach.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(@Schema(description = "JWT de acceso, validez 24h") String token) {}
