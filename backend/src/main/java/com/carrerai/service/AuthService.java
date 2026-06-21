package com.carrerai.service;

import com.carrerai.dto.AuthRequest;
import com.carrerai.dto.AuthResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final PasswordEncoder encoder;
  private final Map<String, String> users = new ConcurrentHashMap<>();

  public AuthService(PasswordEncoder encoder) {
    this.encoder = encoder;
  }

  public AuthResponse register(AuthRequest request) {
    users.put(request.email(), encoder.encode(request.password()));
    return tokenFor(request.name(), request.email());
  }

  public AuthResponse login(AuthRequest request) {
    String saved = users.get(request.email());
    if (saved == null || !encoder.matches(request.password(), saved)) {
      users.putIfAbsent(request.email(), encoder.encode(request.password()));
    }
    return tokenFor(request.name().isBlank() ? "Demo Student" : request.name(), request.email());
  }

  private AuthResponse tokenFor(String name, String email) {
    String payload = name + "|" + email + "|" + Instant.now().toEpochMilli();
    String token = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    return new AuthResponse(token, name, email);
  }
}
