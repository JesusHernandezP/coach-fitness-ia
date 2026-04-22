package com.fitnesscoach.exception;

import com.fitnesscoach.chat.RateLimitException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
      ConflictException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(HttpServletRequest req) {
    return build(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas", req.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      EntityNotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(RateLimitException.class)
  public ResponseEntity<ErrorResponse> handleRateLimit(
      RateLimitException ex, HttpServletRequest req) {
    return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception on {}", req.getRequestURI(), ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path) {
    return ResponseEntity.status(status)
        .body(
            new ErrorResponse(
                Instant.now().toString(), status.value(), status.getReasonPhrase(), message, path));
  }

  public record ErrorResponse(
      String timestamp, int status, String error, String message, String path) {}
}
