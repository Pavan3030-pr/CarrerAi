package com.carrerai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.carrerai.dto.ProfileRequest;
import com.carrerai.dto.ResumeRequest;
import com.carrerai.dto.InterviewRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CareerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void health_returnsOk() throws Exception {
    mockMvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("CareerCompass AI backend is running"));
  }

  @Test
  void careerPlan_returnsPlan() throws Exception {
    ProfileRequest req = new ProfileRequest(
        "Pavan", "B.Tech CSE", "Java Full Stack Developer", "Student",
        List.of("Java", "Spring Boot", "React", "SQL", "Git"),
        List.of("AI", "Web development"));

    mockMvc.perform(post("/api/career-plan")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readinessScore").isNumber())
        .andExpect(jsonPath("$.careerMatches").isArray())
        .andExpect(jsonPath("$.skillGaps").isArray())
        .andExpect(jsonPath("$.roadmap").isArray())
        .andExpect(jsonPath("$.nextActions").isArray());
  }

  @Test
  void careerPlan_withMinimalProfile() throws Exception {
    ProfileRequest req = new ProfileRequest(
        "Test", "", "Developer", "Student", List.of(), List.of());

    mockMvc.perform(post("/api/career-plan")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readinessScore").isNumber());
  }

  @Test
  void resumeAnalyze_returnsAnalysis() throws Exception {
    ResumeRequest req = new ResumeRequest("Java Full Stack Developer",
        "Java Spring Boot React SQL REST API developer with project experience.");

    mockMvc.perform(post("/api/resume/analyze")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.atsScore").isNumber())
        .andExpect(jsonPath("$.contentScore").isNumber())
        .andExpect(jsonPath("$.impactScore").isNumber())
        .andExpect(jsonPath("$.strengths").isArray())
        .andExpect(jsonPath("$.fixes").isArray())
        .andExpect(jsonPath("$.rewriteSuggestions").isArray());
  }

  @Test
  void interviewScore_returnsFeedback() throws Exception {
    InterviewRequest req = new InterviewRequest("Java Full Stack Developer",
        "Tell me about a project",
        "I built REST APIs with Spring Boot and connected them to a React frontend. I owned the dashboard module.");

    mockMvc.perform(post("/api/interview/score")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").isNumber())
        .andExpect(jsonPath("$.question").isString())
        .andExpect(jsonPath("$.verdict").isString())
        .andExpect(jsonPath("$.coachingTips").isArray())
        .andExpect(jsonPath("$.improvedAnswer").isString());
  }

  @Test
  void careerPlan_returnsTopMatchAsJavaFullStack() throws Exception {
    ProfileRequest req = new ProfileRequest(
        "Pavan", "B.Tech", "Java Full Stack Developer", "Student",
        List.of("Java", "Spring Boot", "React", "SQL"),
        List.of());

    mockMvc.perform(post("/api/career-plan")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.careerMatches[0].role").value("Java Full Stack Developer"))
        .andExpect(jsonPath("$.careerMatches[0].fit").isNumber());
  }
}
