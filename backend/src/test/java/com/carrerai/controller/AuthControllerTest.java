package com.carrerai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.carrerai.dto.AuthRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void register_returnsToken() throws Exception {
    AuthRequest req = new AuthRequest("Test User", "test@example.com", "password123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  void register_returnsBadRequestForDuplicateEmail() throws Exception {
    AuthRequest req = new AuthRequest("User", "dup@test.com", "pass123");

    // First registration
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk());

    // Duplicate registration
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_succeedsAfterRegistration() throws Exception {
    // Register first
    AuthRequest registerReq = new AuthRequest("Test User", "login@test.com", "mypassword");
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerReq)))
        .andExpect(status().isOk());

    // Login
    AuthRequest loginReq = new AuthRequest("Test User", "login@test.com", "mypassword");
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.email").value("login@test.com"));
  }

  @Test
  void login_returnsBadRequestForInvalidPassword() throws Exception {
    // Register first
    AuthRequest registerReq = new AuthRequest("Test User", "badpw@test.com", "correctpass");
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerReq)))
        .andExpect(status().isOk());

    // Login with wrong password
    AuthRequest loginReq = new AuthRequest("", "badpw@test.com", "wrongpass");
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_returnsBadRequestForUnregisteredUser() throws Exception {
    AuthRequest req = new AuthRequest("", "unknown@test.com", "somepass");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_returnsBadRequestForMissingFields() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
