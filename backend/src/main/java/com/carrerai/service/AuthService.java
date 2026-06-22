package com.carrerai.service;

import com.carrerai.dto.AuthRequest;
import com.carrerai.dto.AuthResponse;
import com.carrerai.util.JwtUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final PasswordEncoder encoder;
  private final JwtUtil jwtUtil;
  private final Map<String, UserRecord> users = new ConcurrentHashMap<>();

  public AuthService(PasswordEncoder encoder, JwtUtil jwtUtil) {
    this.encoder = encoder;
    this.jwtUtil = jwtUtil;
  }

  public AuthResponse register(AuthRequest request) {
    String email = normalizeEmail(request.email());
    if (users.containsKey(email)) {
      throw new IllegalArgumentException("Email already registered: " + email);
    }
    users.put(email, new UserRecord(encoder.encode(request.password()), request.name()));
    String token = jwtUtil.generateToken(request.name(), email);
    return new AuthResponse(token, request.name(), email);
  }

  public AuthResponse login(AuthRequest request) {
    String email = normalizeEmail(request.email());
    UserRecord record = users.get(email);
    if (record == null || !encoder.matches(request.password(), record.passwordHash())) {
      throw new IllegalArgumentException("Invalid email or password");
    }
    String token = jwtUtil.generateToken(record.name(), email);
    return new AuthResponse(token, record.name(), email);
  }

  private String normalizeEmail(String email) {
    return email == null ? null : email.trim().toLowerCase();
  }

  private record UserRecord(String passwordHash, String name) {}
}
