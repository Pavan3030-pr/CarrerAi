package com.carrerai.service;

import static org.junit.jupiter.api.Assertions.*;

import com.carrerai.dto.InterviewRequest;
import com.carrerai.dto.ProfileRequest;
import com.carrerai.dto.ResumeRequest;
import com.carrerai.model.CareerPlanResponse;
import com.carrerai.model.InterviewFeedback;
import com.carrerai.model.ResumeAnalysis;
import java.util.List;
import org.junit.jupiter.api.Test;

class CareerAiServiceTest {

  private final CareerAiService service = new CareerAiService();

  @Test
  void buildPlan_returnsReadinessScore() {
    ProfileRequest req = new ProfileRequest(
        "Pavan", "B.Tech CSE", "Java Full Stack Developer", "Student",
        List.of("Java", "Spring Boot", "React", "SQL", "Git"),
        List.of("AI", "Web development"));

    CareerPlanResponse plan = service.buildPlan(req);

    assertTrue(plan.readinessScore() >= 0 && plan.readinessScore() <= 100);
    assertFalse(plan.careerMatches().isEmpty());
    assertFalse(plan.skillGaps().isEmpty());
    assertFalse(plan.roadmap().isEmpty());
    assertFalse(plan.nextActions().isEmpty());
    assertEquals("Java Full Stack Developer", plan.careerMatches().get(0).role());
  }

  @Test
  void buildPlan_sortsCareerMatchesByFitDescending() {
    ProfileRequest req = new ProfileRequest(
        "Test", "B.Tech", "Developer", "Student",
        List.of("Java", "Spring Boot", "React"), List.of());

    CareerPlanResponse plan = service.buildPlan(req);

    for (int i = 1; i < plan.careerMatches().size(); i++) {
      assertTrue(plan.careerMatches().get(i - 1).fit() >= plan.careerMatches().get(i).fit());
    }
  }

  @Test
  void buildPlan_handlesNullSkills() {
    ProfileRequest req = new ProfileRequest(
        "Test", "B.Tech", "Developer", "Student", null, null);

    CareerPlanResponse plan = service.buildPlan(req);

    assertNotNull(plan);
    assertFalse(plan.skillGaps().isEmpty());
  }

  @Test
  void buildPlan_handlesEmptySkills() {
    ProfileRequest req = new ProfileRequest(
        "Test", "B.Tech", "Developer", "Student", List.of(), List.of());

    CareerPlanResponse plan = service.buildPlan(req);

    assertNotNull(plan);
    assertTrue(plan.readinessScore() < 80); // lower readiness with no skills
  }

  @Test
  void analyzeResume_returnsScores() {
    ResumeRequest req = new ResumeRequest("Java Full Stack Developer",
        "Experienced Java developer with Spring Boot, React, SQL, and REST API projects.");

    ResumeAnalysis analysis = service.analyzeResume(req);

    assertTrue(analysis.atsScore() > 0);
    assertTrue(analysis.contentScore() > 0);
    assertTrue(analysis.impactScore() > 0);
    assertFalse(analysis.strengths().isEmpty());
    assertFalse(analysis.fixes().isEmpty());
    assertFalse(analysis.rewriteSuggestions().isEmpty());
  }

  @Test
  void analyzeResume_handlesNullText() {
    ResumeRequest req = new ResumeRequest("Developer", null);

    ResumeAnalysis analysis = service.analyzeResume(req);

    assertNotNull(analysis);
    assertTrue(analysis.atsScore() >= 0);
  }

  @Test
  void scoreInterview_returnsFeedback() {
    InterviewRequest req = new InterviewRequest("Java Full Stack Developer",
        "Tell me about a project",
        "In my recent project, I used Spring Boot and React. The situation was challenging. My task was to build the backend. I took action by designing REST APIs. The result was a working demo.");

    InterviewFeedback feedback = service.scoreInterview(req);

    assertTrue(feedback.score() >= 0 && feedback.score() <= 100);
    assertNotNull(feedback.question());
    assertNotNull(feedback.verdict());
    assertFalse(feedback.coachingTips().isEmpty());
    assertNotNull(feedback.improvedAnswer());
  }

  @Test
  void scoreInterview_handlesNullAnswer() {
    InterviewRequest req = new InterviewRequest("Developer", "Question", null);

    InterviewFeedback feedback = service.scoreInterview(req);

    assertNotNull(feedback);
    assertTrue(feedback.score() >= 0);
  }

  @Test
  void scoreInterview_givesHigherScoreForStarAnswer() {
    InterviewRequest starReq = new InterviewRequest("Developer", "Question",
        "The situation was critical. My task was to build an API. I took action by implementing features. The result was a 30% improvement.");

    InterviewRequest basicReq = new InterviewRequest("Developer", "Question",
        "I built an API and it was fine.");

    InterviewFeedback starFeedback = service.scoreInterview(starReq);
    InterviewFeedback basicFeedback = service.scoreInterview(basicReq);

    assertTrue(starFeedback.score() > basicFeedback.score(),
        "STAR answers should score higher than basic answers");
  }
}
