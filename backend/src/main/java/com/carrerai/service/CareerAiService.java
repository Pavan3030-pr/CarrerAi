package com.carrerai.service;

import com.carrerai.dto.InterviewRequest;
import com.carrerai.dto.ProfileRequest;
import com.carrerai.dto.ResumeRequest;
import com.carrerai.model.CareerPlanResponse;
import com.carrerai.model.CareerScore;
import com.carrerai.model.GapScore;
import com.carrerai.model.InterviewFeedback;
import com.carrerai.model.ResumeAnalysis;
import com.carrerai.model.RoadmapWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CareerAiService {
  private static final List<String> CORE_SKILLS = List.of(
      "Java", "Spring Boot", "React", "SQL", "DSA", "REST APIs", "Git", "Cloud", "Communication");

  public CareerPlanResponse buildPlan(ProfileRequest request) {
    Set<String> known = request.skills() == null
        ? Set.of()
        : request.skills().stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());

    List<GapScore> gaps = CORE_SKILLS.stream()
        .map(skill -> gapFor(skill, known))
        .sorted(Comparator.comparing(GapScore::readiness))
        .toList();

    int readiness = (int) Math.round(gaps.stream().mapToInt(GapScore::readiness).average().orElse(42));
    List<CareerScore> matches = careerMatches(request, known);
    List<RoadmapWeek> roadmap = roadmapFor(request.targetRole(), gaps);

    return new CareerPlanResponse(
        readiness,
        matches,
        gaps,
        roadmap,
        List.of(
            "Complete the first two roadmap weeks and upload proof of work.",
            "Rewrite resume bullets with numbers, verbs, and role keywords.",
            "Run five mock interviews and push the average score above 65.")
    );
  }

  public ResumeAnalysis analyzeResume(ResumeRequest request) {
    String text = request.resumeText() == null ? "" : request.resumeText();
    int lengthBoost = Math.min(22, text.length() / 80);
    int keywordBoost = containsAny(text, "spring", "react", "sql", "project", "api") ? 18 : 5;
    int metricBoost = text.matches("(?s).*\\d+.*") ? 16 : 4;
    int ats = clamp(45 + lengthBoost + keywordBoost, 0, 96);
    int impact = clamp(38 + metricBoost + lengthBoost, 0, 95);
    int content = clamp((ats + impact) / 2 + 7, 0, 97);

    return new ResumeAnalysis(
        ats,
        content,
        impact,
        List.of("Clear target role alignment", "Project experience is visible", "Technical keywords are present"),
        List.of("Add measurable outcomes to every project", "Move core skills into a scannable section", "Add one deployed link or GitHub proof"),
        List.of(
            "Built a Spring Boot and React platform that improved career readiness tracking with real-time scoring.",
            "Designed REST APIs for assessment, resume analysis, interview feedback, and personalized roadmaps.",
            "Delivered an end-to-end AI career workflow replacing fragmented preparation tools.")
    );
  }

  public InterviewFeedback scoreInterview(InterviewRequest request) {
    String answer = request.answer() == null ? "" : request.answer();
    int structure = containsAny(answer, "situation", "task", "action", "result") ? 22 : 10;
    int specificity = answer.matches("(?s).*\\d+.*") ? 20 : 8;
    int relevance = containsAny(answer, request.targetRole(), "project", "team", "learned") ? 24 : 14;
    int score = clamp(35 + structure + specificity + relevance, 0, 98);

    return new InterviewFeedback(
        score,
        request.question(),
        score >= 70 ? "Strong answer. Add one sharper metric to make it placement-ready." : "Good start. Structure it using STAR and add proof.",
        List.of("Open with context in one sentence", "Show your exact contribution", "End with measurable result or learning"),
        "In my recent project, I owned the backend API flow, designed the service layer, and connected it to the frontend. The result was a working demo with authentication, scoring, and dashboards, which made the product easier for judges to evaluate."
    );
  }

  private GapScore gapFor(String skill, Set<String> known) {
    boolean hasSkill = known.contains(skill.toLowerCase());
    int readiness = hasSkill ? 82 : switch (skill) {
      case "DSA", "Cloud", "Communication" -> 46;
      case "Spring Boot", "React", "SQL" -> 54;
      default -> 60;
    };
    String priority = readiness < 50 ? "Critical" : readiness < 70 ? "Improve" : "Ready";
    String recommendation = readiness < 70
        ? "Build one mini-project or practice set focused on " + skill + "."
        : "Use this as a resume strength and interview proof point.";
    return new GapScore(skill, readiness, priority, recommendation);
  }

  private List<CareerScore> careerMatches(ProfileRequest request, Set<String> known) {
    List<CareerScore> scores = new ArrayList<>();
    scores.add(new CareerScore("Java Full Stack Developer", score(known, "java", "spring boot", "react", "sql"), "Best match for Spring, REST, React, and SQL skills."));
    scores.add(new CareerScore("Backend Engineer", score(known, "java", "spring boot", "sql", "rest apis"), "Strong if you enjoy APIs, databases, and services."));
    scores.add(new CareerScore("Frontend Engineer", score(known, "react", "git", "communication"), "Good path if UI building and product thinking are strong."));
    scores.add(new CareerScore("Cloud Engineer", score(known, "cloud", "sql", "git"), "Future path after strengthening cloud deployments."));
    scores.add(new CareerScore("Data Analyst", score(known, "sql", "communication"), "Possible path if analytics projects are added."));
    return scores.stream().sorted(Comparator.comparing(CareerScore::fit).reversed()).toList();
  }

  private List<RoadmapWeek> roadmapFor(String targetRole, List<GapScore> gaps) {
    String role = targetRole == null || targetRole.isBlank() ? "Java Full Stack Developer" : targetRole;
    return List.of(
        new RoadmapWeek(1, "Career baseline for " + role, List.of("Finish assessment", "Pick top 3 target companies", "Set readiness goal"), "Saved profile and baseline score"),
        new RoadmapWeek(2, "Core skill gaps", List.of("Practice " + gaps.get(0).skill(), "Practice " + gaps.get(1).skill(), "Document mistakes"), "Gap matrix improved by 10 points"),
        new RoadmapWeek(3, "Project proof", List.of("Build one feature", "Write API documentation", "Deploy demo"), "GitHub repository with README"),
        new RoadmapWeek(4, "Resume intelligence", List.of("Rewrite bullets", "Add metrics", "Run ATS review"), "Resume score above 75"),
        new RoadmapWeek(5, "Interview loop", List.of("Answer 10 role questions", "Record weak areas", "Repeat top 3 questions"), "Mock interview average above 65"),
        new RoadmapWeek(6, "Placement sprint", List.of("Apply to 20 roles", "Customize resume", "Track responses"), "Application tracker and interview shortlist")
    );
  }

  private int score(Set<String> known, String... skills) {
    int matches = 0;
    for (String skill : skills) {
      if (known.contains(skill)) {
        matches++;
      }
    }
    return clamp(48 + matches * 13, 0, 97);
  }

  private boolean containsAny(String text, String... words) {
    String lower = text == null ? "" : text.toLowerCase();
    for (String word : words) {
      if (word != null && !word.isBlank() && lower.contains(word.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
