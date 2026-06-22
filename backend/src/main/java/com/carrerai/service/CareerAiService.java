package com.carrerai.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CareerAiService {

  private static final Logger log = LoggerFactory.getLogger(CareerAiService.class);
  private static final List<String> CORE_SKILLS = List.of(
      "Java", "Spring Boot", "React", "SQL", "DSA", "REST APIs", "Git", "Cloud", "Communication");

  private final GeminiService gemini;

  public CareerAiService(GeminiService gemini) {
    this.gemini = gemini;
  }

  // --------------------------------------------------------------------------
  // Career Plan (assessment + gaps + roadmap)
  // --------------------------------------------------------------------------

  public CareerPlanResponse buildPlan(ProfileRequest request) {
    // Try Gemini first
    if (gemini.isAvailable()) {
      try {
        JsonNode ai = gemini.generateStructured(
            careerPlanSystemPrompt(),
            careerPlanUserPrompt(request));
        if (ai != null) {
          return parseCareerPlan(ai, request);
        }
      } catch (Exception e) {
        log.warn("Gemini career-plan failed, using mock: {}", e.getMessage());
      }
    }
    return mockCareerPlan(request);
  }

  // --------------------------------------------------------------------------
  // Resume Analysis
  // --------------------------------------------------------------------------

  public ResumeAnalysis analyzeResume(ResumeRequest request) {
    if (gemini.isAvailable()) {
      try {
        JsonNode ai = gemini.generateStructured(
            resumeAnalysisSystemPrompt(),
            resumeAnalysisUserPrompt(request));
        if (ai != null) {
          return parseResumeAnalysis(ai);
        }
      } catch (Exception e) {
        log.warn("Gemini resume analysis failed, using mock: {}", e.getMessage());
      }
    }
    return mockResumeAnalysis(request);
  }

  // --------------------------------------------------------------------------
  // Interview Scoring
  // --------------------------------------------------------------------------

  public InterviewFeedback scoreInterview(InterviewRequest request) {
    if (gemini.isAvailable()) {
      try {
        JsonNode ai = gemini.generateStructured(
            interviewSystemPrompt(),
            interviewUserPrompt(request));
        if (ai != null) {
          return parseInterviewFeedback(ai, request);
        }
      } catch (Exception e) {
        log.warn("Gemini interview scoring failed, using mock: {}", e.getMessage());
      }
    }
    return mockInterviewFeedback(request);
  }

  // ==========================================================================
  // Gemini prompt templates
  // ==========================================================================

  private String careerPlanSystemPrompt() {
    return "You are an expert career coach for Indian engineering students. "
        + "Analyze the student's profile and return a JSON object with this exact structure:\n"
        + "{\n"
        + "  \"readinessScore\": 0-100 integer,\n"
        + "  \"careerMatches\": [{\"role\": string, \"fit\": 0-100, \"reason\": string}],\n"
        + "  \"skillGaps\": [{\"skill\": string, \"readiness\": 0-100, \"priority\": \"Critical|Improve|Ready\", \"recommendation\": string}],\n"
        + "  \"roadmap\": [{\"week\": number, \"focus\": string, \"actions\": [string], \"proofOfWork\": string}],\n"
        + "  \"nextActions\": [string]\n"
        + "}\n"
        + "Be specific to the student's skills and target role. Provide actionable recommendations.";
  }

  private String careerPlanUserPrompt(ProfileRequest req) {
    return String.format(
        "Student profile:\n- Name: %s\n- Degree: %s\n- Target Role: %s\n- Experience: %s\n- Skills: %s\n- Interests: %s\n\n"
        + "Generate a personalized career readiness plan.",
        req.name(), req.degree(), req.targetRole(), req.experienceLevel(),
        req.skills(), req.interests());
  }

  private String resumeAnalysisSystemPrompt() {
    return "You are an expert ATS resume reviewer. "
        + "Analyze the resume text for a given target role. "
        + "Return JSON with this exact structure:\n"
        + "{\n"
        + "  \"atsScore\": 0-100 integer,\n"
        + "  \"contentScore\": 0-100 integer,\n"
        + "  \"impactScore\": 0-100 integer,\n"
        + "  \"strengths\": [string],\n"
        + "  \"fixes\": [string],\n"
        + "  \"rewriteSuggestions\": [string]\n"
        + "}\n"
        + "Be honest and constructive. Focus on ATS keywords, measurable impact, and content quality.";
  }

  private String resumeAnalysisUserPrompt(ResumeRequest req) {
    return String.format(
        "Target Role: %s\n\nResume Text:\n%s\n\nAnalyze this resume and provide scores and actionable feedback.",
        req.targetRole(), req.resumeText());
  }

  private String interviewSystemPrompt() {
    return "You are an expert interview coach conducting a mock interview. "
        + "Score the candidate's answer for a given question and target role. "
        + "Return JSON with this exact structure:\n"
        + "{\n"
        + "  \"score\": 0-100 integer,\n"
        + "  \"verdict\": string (2-3 sentence feedback),\n"
        + "  \"coachingTips\": [string] (3-4 specific tips),\n"
        + "  \"improvedAnswer\": string (a rewritten STAR-format answer)\n"
        + "}\n"
        + "Evaluate structure (STAR), specificity (metrics), and relevance to the role.";
  }

  private String interviewUserPrompt(InterviewRequest req) {
    return String.format(
        "Target Role: %s\nQuestion: %s\n\nCandidate's Answer:\n%s\n\nScore and provide coaching feedback.",
        req.targetRole(), req.question(), req.answer());
  }

  // ==========================================================================
  // Gemini response parsers
  // ==========================================================================

  private CareerPlanResponse parseCareerPlan(JsonNode ai, ProfileRequest req) {
    List<CareerScore> matches = new ArrayList<>();
    JsonNode cm = ai.get("careerMatches");
    if (cm != null && cm.isArray()) {
      for (JsonNode m : cm) {
        matches.add(new CareerScore(
            getStr(m, "role"),
            getInt(m, "fit", 50),
            getStr(m, "reason")));
      }
    }

    List<GapScore> gaps = new ArrayList<>();
    JsonNode sg = ai.get("skillGaps");
    if (sg != null && sg.isArray()) {
      for (JsonNode g : sg) {
        gaps.add(new GapScore(
            getStr(g, "skill"),
            getInt(g, "readiness", 50),
            getStr(g, "priority"),
            getStr(g, "recommendation")));
      }
    }

    List<RoadmapWeek> roadmap = new ArrayList<>();
    JsonNode rw = ai.get("roadmap");
    if (rw != null && rw.isArray()) {
      for (JsonNode w : rw) {
        List<String> actions = new ArrayList<>();
        JsonNode acts = w.get("actions");
        if (acts != null && acts.isArray()) {
          acts.forEach(a -> actions.add(a.asText()));
        }
        roadmap.add(new RoadmapWeek(
            getInt(w, "week", 1),
            getStr(w, "focus"),
            actions,
            getStr(w, "proofOfWork")));
      }
    }

    List<String> nextActions = new ArrayList<>();
    JsonNode na = ai.get("nextActions");
    if (na != null && na.isArray()) {
      na.forEach(a -> nextActions.add(a.asText()));
    }

    return new CareerPlanResponse(
        getInt(ai, "readinessScore", 50),
        matches.isEmpty() ? mockCareerMatches(req) : matches,
        gaps.isEmpty() ? mockSkillGaps(req) : gaps,
        roadmap.isEmpty() ? mockRoadmap(req) : roadmap,
        nextActions.isEmpty() ? List.of("Complete assessment", "Review skill gaps", "Start week 1 roadmap") : nextActions);
  }

  private ResumeAnalysis parseResumeAnalysis(JsonNode ai) {
    List<String> strengths = new ArrayList<>();
    JsonNode s = ai.get("strengths");
    if (s != null && s.isArray()) s.forEach(n -> strengths.add(n.asText()));

    List<String> fixes = new ArrayList<>();
    JsonNode f = ai.get("fixes");
    if (f != null && f.isArray()) f.forEach(n -> fixes.add(n.asText()));

    List<String> rewrites = new ArrayList<>();
    JsonNode r = ai.get("rewriteSuggestions");
    if (r != null && r.isArray()) r.forEach(n -> rewrites.add(n.asText()));

    return new ResumeAnalysis(
        getInt(ai, "atsScore", 70),
        getInt(ai, "contentScore", 70),
        getInt(ai, "impactScore", 70),
        strengths.isEmpty() ? List.of("Technical keywords are visible") : strengths,
        fixes.isEmpty() ? List.of("Add measurable outcomes") : fixes,
        rewrites.isEmpty() ? List.of("Focus on quantifiable achievements") : rewrites);
  }

  private InterviewFeedback parseInterviewFeedback(JsonNode ai, InterviewRequest req) {
    List<String> tips = new ArrayList<>();
    JsonNode ct = ai.get("coachingTips");
    if (ct != null && ct.isArray()) ct.forEach(t -> tips.add(t.asText()));

    return new InterviewFeedback(
        getInt(ai, "score", 60),
        req.question(),
        getStr(ai, "verdict"),
        tips.isEmpty() ? List.of("Use STAR structure", "Add metrics") : tips,
        getStr(ai, "improvedAnswer"));
  }

  // ==========================================================================
  // Mock fallback logic (same as before)
  // ==========================================================================

  private CareerPlanResponse mockCareerPlan(ProfileRequest request) {
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
        readiness, matches, gaps, roadmap,
        List.of("Complete the first two roadmap weeks and upload proof of work.",
            "Rewrite resume bullets with numbers, verbs, and role keywords.",
            "Run five mock interviews and push the average score above 65."));
  }

  private ResumeAnalysis mockResumeAnalysis(ResumeRequest request) {
    String text = request.resumeText() == null ? "" : request.resumeText();
    int lengthBoost = Math.min(22, text.length() / 80);
    int keywordBoost = containsAny(text, "spring", "react", "sql", "project", "api") ? 18 : 5;
    int metricBoost = text.matches("(?s).*\\d+.*") ? 16 : 4;
    int ats = clamp(45 + lengthBoost + keywordBoost, 0, 96);
    int impact = clamp(38 + metricBoost + lengthBoost, 0, 95);
    int content = clamp((ats + impact) / 2 + 7, 0, 97);

    return new ResumeAnalysis(ats, content, impact,
        List.of("Clear target role alignment", "Project experience is visible", "Technical keywords are present"),
        List.of("Add measurable outcomes to every project", "Move core skills into a scannable section", "Add one deployed link or GitHub proof"),
        List.of("Built a Spring Boot and React platform that improved career readiness tracking with real-time scoring.",
            "Designed REST APIs for assessment, resume analysis, interview feedback, and personalized roadmaps.",
            "Delivered an end-to-end AI career workflow replacing fragmented preparation tools."));
  }

  private InterviewFeedback mockInterviewFeedback(InterviewRequest request) {
    String answer = request.answer() == null ? "" : request.answer();
    int structure = containsAny(answer, "situation", "task", "action", "result") ? 22 : 10;
    int specificity = answer.matches("(?s).*\\d+.*") ? 20 : 8;
    int relevance = containsAny(answer, request.targetRole(), "project", "team", "learned") ? 24 : 14;
    int score = clamp(35 + structure + specificity + relevance, 0, 98);

    return new InterviewFeedback(score, request.question(),
        score >= 70 ? "Strong answer. Add one sharper metric to make it placement-ready."
            : "Good start. Structure it using STAR and add proof.",
        List.of("Open with context in one sentence", "Show your exact contribution", "End with measurable result or learning"),
        "In my recent project, I owned the backend API flow, designed the service layer, and connected it to the frontend. The result was a working demo with authentication, scoring, and dashboards, which made the product easier for judges to evaluate.");
  }

  // ==========================================================================
  // Shared helpers
  // ==========================================================================

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
    return mockCareerMatches(request);
  }

  private List<CareerScore> mockCareerMatches(ProfileRequest request) {
    Set<String> known = request.skills() == null
        ? Set.of()
        : request.skills().stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
    List<CareerScore> scores = new ArrayList<>();
    scores.add(new CareerScore("Java Full Stack Developer", score(known, "java", "spring boot", "react", "sql"), "Best match for Spring, REST, React, and SQL skills."));
    scores.add(new CareerScore("Backend Engineer", score(known, "java", "spring boot", "sql", "rest apis"), "Strong if you enjoy APIs, databases, and services."));
    scores.add(new CareerScore("Frontend Engineer", score(known, "react", "git", "communication"), "Good path if UI building and product thinking are strong."));
    scores.add(new CareerScore("Cloud Engineer", score(known, "cloud", "sql", "git"), "Future path after strengthening cloud deployments."));
    scores.add(new CareerScore("Data Analyst", score(known, "sql", "communication"), "Possible path if analytics projects are added."));
    return scores.stream().sorted(Comparator.comparing(CareerScore::fit).reversed()).toList();
  }

  private List<GapScore> mockSkillGaps(ProfileRequest req) {
    Set<String> known = req.skills() == null
        ? Set.of()
        : req.skills().stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
    return CORE_SKILLS.stream()
        .map(skill -> gapFor(skill, known))
        .sorted(Comparator.comparing(GapScore::readiness))
        .toList();
  }

  private List<RoadmapWeek> mockRoadmap(ProfileRequest req) {
    return roadmapFor(req.targetRole(), mockSkillGaps(req));
  }

  private List<RoadmapWeek> roadmapFor(String targetRole, List<GapScore> gaps) {
    String role = targetRole == null || targetRole.isBlank() ? "Java Full Stack Developer" : targetRole;
    return List.of(
        new RoadmapWeek(1, "Career baseline for " + role, List.of("Finish assessment", "Pick top 3 target companies", "Set readiness goal"), "Saved profile and baseline score"),
        new RoadmapWeek(2, "Core skill gaps", List.of("Practice " + gaps.get(0).skill(), "Practice " + gaps.get(1).skill(), "Document mistakes"), "Gap matrix improved by 10 points"),
        new RoadmapWeek(3, "Project proof", List.of("Build one feature", "Write API documentation", "Deploy demo"), "GitHub repository with README"),
        new RoadmapWeek(4, "Resume intelligence", List.of("Rewrite bullets", "Add metrics", "Run ATS review"), "Resume score above 75"),
        new RoadmapWeek(5, "Interview loop", List.of("Answer 10 role questions", "Record weak areas", "Repeat top 3 questions"), "Mock interview average above 65"),
        new RoadmapWeek(6, "Placement sprint", List.of("Apply to 20 roles", "Customize resume", "Track responses"), "Application tracker and interview shortlist"));
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

  private String getStr(JsonNode node, String field) {
    JsonNode n = node.get(field);
    return n == null ? "" : n.asText();
  }

  private int getInt(JsonNode node, String field, int def) {
    JsonNode n = node.get(field);
    return n == null ? def : n.asInt(def);
  }
}
