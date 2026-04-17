package com.fitnesscoach.chat;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitException extends RuntimeException {
  public RateLimitException() {
    super("Limite de 20 mensajes por hora alcanzado");
  }
}
