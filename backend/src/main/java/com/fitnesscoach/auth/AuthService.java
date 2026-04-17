package com.fitnesscoach.auth;

import com.fitnesscoach.exception.ConflictException;
import com.fitnesscoach.security.JwtService;
import com.fitnesscoach.user.User;
import com.fitnesscoach.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(AuthRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email ya registrado");
    }
    User user =
        User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .build();
    userRepository.save(user);
    return new AuthResponse(jwtService.generateToken(user));
  }

  public AuthResponse login(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    User user = userRepository.findByEmail(request.email()).orElseThrow();
    return new AuthResponse(jwtService.generateToken(user));
  }
}
