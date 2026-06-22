package com.carrerai.controller;

import com.carrerai.model.CareerPlanEntity;
import com.carrerai.model.InterviewFeedbackEntity;
import com.carrerai.model.ResumeAnalysisEntity;
import com.carrerai.model.UserEntity;
import com.carrerai.repository.CareerPlanRepository;
import com.carrerai.repository.InterviewFeedbackRepository;
import com.carrerai.repository.ResumeAnalysisRepository;
import com.carrerai.repository.UserRepository;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final UserRepository userRepo;
  private final CareerPlanRepository careerPlanRepo;
  private final ResumeAnalysisRepository resumeRepo;
  private final InterviewFeedbackRepository interviewRepo;

  public AdminController(UserRepository userRepo, CareerPlanRepository careerPlanRepo,
      ResumeAnalysisRepository resumeRepo, InterviewFeedbackRepository interviewRepo) {
    this.userRepo = userRepo;
    this.careerPlanRepo = careerPlanRepo;
    this.resumeRepo = resumeRepo;
    this.interviewRepo = interviewRepo;
  }

  @GetMapping("/users")
  List<UserEntity> listUsers() {
    return userRepo.findAll();
  }

  @GetMapping("/analytics/overview")
  Map<String, Object> overview() {
    Map<String, Object> stats = new LinkedHashMap<>();
    stats.put("totalUsers", userRepo.count());
    stats.put("totalCareerPlans", careerPlanRepo.count());
    stats.put("totalResumeAnalyses", resumeRepo.count());
    stats.put("totalInterviewFeedbacks", interviewRepo.count());

    Double avgReadiness = careerPlanRepo.averageReadinessScore();
    stats.put("averageReadinessScore", avgReadiness != null ? Math.round(avgReadiness) : 0);

    Double avgInterviewScore = interviewRepo.averageScore();
    stats.put("averageInterviewScore", avgInterviewScore != null ? Math.round(avgInterviewScore) : 0);

    // Recent signups (last 7 days)
    long recentUsers = userRepo.findAll().stream()
        .filter(u -> u.getCreatedAt() != null
            && u.getCreatedAt().isAfter(java.time.Instant.now().minus(java.time.Duration.ofDays(7))))
        .count();
    stats.put("recentSignups7Days", recentUsers);

    return stats;
  }

  @GetMapping("/analytics/recent-plans")
  List<CareerPlanEntity> recentPlans(@RequestParam(defaultValue = "10") int limit) {
    List<CareerPlanEntity> all = careerPlanRepo.findAll();
    all.sort(Comparator.comparing(CareerPlanEntity::getCreatedAt).reversed());
    return all.subList(0, Math.min(limit, all.size()));
  }

  @GetMapping("/analytics/user-history")
  Map<String, Object> userHistory(@RequestParam(required = false) String email) {
    Map<String, Object> result = new LinkedHashMap<>();
    if (email == null || email.isBlank()) {
      result.put("error", "Email parameter is required");
      return result;
    }
    result.put("user", userRepo.findByEmail(email).orElse(null));
    result.put("careerPlans", careerPlanRepo.findByUserEmailOrderByCreatedAtDesc(email));
    result.put("resumeAnalyses", resumeRepo.findByUserEmailOrderByCreatedAtDesc(email));
    result.put("interviewFeedbacks", interviewRepo.findByUserEmailOrderByCreatedAtDesc(email));
    return result;
  }
}
