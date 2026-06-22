package com.carrerai.service;

import com.carrerai.dto.AuthRequest;
import com.carrerai.dto.AuthResponse;
import com.carrerai.model.UserEntity;
import com.carrerai.repository.UserRepository;
import com.carrerai.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final PasswordEncoder encoder;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  public AuthService(PasswordEncoder encoder, JwtUtil jwtUtil, UserRepository userRepository) {
    this.encoder = encoder;
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  public AuthResponse register(AuthRequest request) {
    String email = normalizeEmail(request.email());
    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already registered: " + email);
    }
    UserEntity user = new UserEntity(request.name(), email, encoder.encode(request.password()));
    userRepository.save(user);
    String token = jwtUtil.generateToken(request.name(), email);
    return new AuthResponse(token, request.name(), email);
  }

  public AuthResponse login(AuthRequest request) {
    String email = normalizeEmail(request.email());
    UserEntity user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    if (!encoder.matches(request.password(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid email or password");
    }
    String token = jwtUtil.generateToken(user.getName(), email);
    return new AuthResponse(token, user.getName(), email);
  }

  private String normalizeEmail(String email) {
    return email == null ? null : email.trim().toLowerCase();
  }
}
