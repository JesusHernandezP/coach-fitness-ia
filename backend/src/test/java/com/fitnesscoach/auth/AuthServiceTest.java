package com.fitnesscoach.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fitnesscoach.exception.ConflictException;
import com.fitnesscoach.security.JwtService;
import com.fitnesscoach.user.User;
import com.fitnesscoach.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;
  @Mock AuthenticationManager authenticationManager;

  @InjectMocks AuthService authService;

  @Test
  void register_returnsToken_whenEmailNotTaken() {
    var request = new AuthRequest("test@example.com", "password123");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("hashed");
    when(userRepository.save(any())).thenReturn(User.builder().email(request.email()).build());
    when(jwtService.generateToken(any())).thenReturn("jwt-token");

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void register_throwsConflict_whenEmailAlreadyExists() {
    var request = new AuthRequest("existing@example.com", "password123");
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("Email ya registrado");
  }
}
