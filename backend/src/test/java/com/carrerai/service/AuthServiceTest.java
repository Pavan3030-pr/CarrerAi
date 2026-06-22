package com.carrerai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.carrerai.dto.AuthRequest;
import com.carrerai.dto.AuthResponse;
import com.carrerai.model.UserEntity;
import com.carrerai.repository.UserRepository;
import com.carrerai.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private PasswordEncoder encoder;
  @Mock private JwtUtil jwtUtil;
  @Mock private UserRepository userRepository;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    encoder = new BCryptPasswordEncoder();
    authService = new AuthService(encoder, jwtUtil, userRepository);
  }

  @Test
  void register_createsUserAndReturnsToken() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(jwtUtil.generateToken("Test User", "test@example.com")).thenReturn("jwt-token");

    AuthResponse response = authService.register(
        new AuthRequest("Test User", "test@example.com", "password123"));

    assertEquals("jwt-token", response.token());
    assertEquals("Test User", response.name());
    assertEquals("test@example.com", response.email());
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  void register_throwsOnDuplicateEmail() {
    when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> authService.register(new AuthRequest("User2", "dup@example.com", "pass2")));
  }

  @Test
  void login_succeedsWithValidCredentials() {
    UserEntity user = new UserEntity("Test User", "test@example.com",
        encoder.encode("password123"));
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken("Test User", "test@example.com")).thenReturn("jwt-token");

    AuthResponse response = authService.login(
        new AuthRequest("", "test@example.com", "password123"));

    assertEquals("jwt-token", response.token());
    assertEquals("Test User", response.name());
    assertEquals("test@example.com", response.email());
  }

  @Test
  void login_throwsOnInvalidEmail() {
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> authService.login(new AuthRequest("", "nonexistent@example.com", "pass")));
  }

  @Test
  void login_throwsOnInvalidPassword() {
    UserEntity user = new UserEntity("User", "test@example.com",
        encoder.encode("correctpass"));
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    assertThrows(IllegalArgumentException.class,
        () -> authService.login(new AuthRequest("", "test@example.com", "wrongpass")));
  }

  @Test
  void login_returnsStoredName() {
    UserEntity user = new UserEntity("Registered Name", "test@example.com",
        encoder.encode("password123"));
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken("Registered Name", "test@example.com")).thenReturn("jwt-token");

    AuthResponse response = authService.login(
        new AuthRequest("Different Name", "test@example.com", "password123"));

    assertEquals("Registered Name", response.name());
  }
}
