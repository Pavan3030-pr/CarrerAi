package com.carrerai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.carrerai.dto.AuthRequest;
import com.carrerai.dto.AuthResponse;
import com.carrerai.util.JwtUtil;
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
  @Mock
  private JwtUtil jwtUtil;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    encoder = new BCryptPasswordEncoder();
    authService = new AuthService(encoder, jwtUtil);
  }

  @Test
  void register_createsUserAndReturnsToken() {
    when(jwtUtil.generateToken("Test User", "test@example.com")).thenReturn("jwt-token");

    AuthResponse response = authService.register(
        new AuthRequest("Test User", "test@example.com", "password123"));

    assertEquals("jwt-token", response.token());
    assertEquals("Test User", response.name());
    assertEquals("test@example.com", response.email());
  }

  @Test
  void register_throwsOnDuplicateEmail() {
    authService.register(new AuthRequest("User1", "dup@example.com", "pass1"));

    assertThrows(IllegalArgumentException.class,
        () -> authService.register(new AuthRequest("User2", "dup@example.com", "pass2")));
  }

  @Test
  void login_succeedsWithValidCredentials() {
    when(jwtUtil.generateToken("Test User", "test@example.com")).thenReturn("jwt-token");

    authService.register(new AuthRequest("Test User", "test@example.com", "password123"));
    AuthResponse response = authService.login(
        new AuthRequest("", "test@example.com", "password123"));

    assertEquals("jwt-token", response.token());
    assertEquals("Test User", response.name()); // name from registration, not login request
    assertEquals("test@example.com", response.email());
  }

  @Test
  void login_throwsOnInvalidEmail() {
    assertThrows(IllegalArgumentException.class,
        () -> authService.login(new AuthRequest("", "nonexistent@example.com", "pass")));
  }

  @Test
  void login_throwsOnInvalidPassword() {
    authService.register(new AuthRequest("User", "test@example.com", "correctpass"));

    assertThrows(IllegalArgumentException.class,
        () -> authService.login(new AuthRequest("", "test@example.com", "wrongpass")));
  }

  @Test
  void login_returnsStoredNameNotLoginRequestName() {
    when(jwtUtil.generateToken("Registered Name", "test@example.com")).thenReturn("jwt-token");

    authService.register(new AuthRequest("Registered Name", "test@example.com", "password123"));

    // Login with a different name should still return the registered name
    AuthResponse response = authService.login(
        new AuthRequest("Different Name", "test@example.com", "password123"));

    assertEquals("Registered Name", response.name());
  }

  @Test
  void register_handlesWhitespaceTrimming() {
    when(jwtUtil.generateToken("Test User", "test@example.com")).thenReturn("jwt-token");

    AuthResponse response = authService.register(
        new AuthRequest("Test User", "  TEST@Example.com  ", "password123"));

    assertEquals("jwt-token", response.token());
    assertEquals("test@example.com", response.email());
  }

  @Test
  void login_worksWithNormalizedEmail() {
    when(jwtUtil.generateToken("Test User", "normalized@test.com")).thenReturn("jwt-token");

    authService.register(new AuthRequest("Test User", "Normalized@Test.com", "pass123"));
    AuthResponse response = authService.login(
        new AuthRequest("", "normalized@test.com", "pass123"));

    assertEquals("Test User", response.name());
    assertEquals("normalized@test.com", response.email());
  }
}
