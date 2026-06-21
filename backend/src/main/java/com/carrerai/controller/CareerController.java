package com.carrerai.controller;

import com.carrerai.dto.InterviewRequest;
import com.carrerai.dto.ProfileRequest;
import com.carrerai.dto.ResumeRequest;
import com.carrerai.model.CareerPlanResponse;
import com.carrerai.model.InterviewFeedback;
import com.carrerai.model.ResumeAnalysis;
import com.carrerai.service.CareerAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CareerController {
  private final CareerAiService careerAiService;

  public CareerController(CareerAiService careerAiService) {
    this.careerAiService = careerAiService;
  }

  @GetMapping("/health")
  String health() {
    return "CareerCompass AI backend is running";
  }

  @PostMapping("/career-plan")
  CareerPlanResponse careerPlan(@RequestBody ProfileRequest request) {
    return careerAiService.buildPlan(request);
  }

  @PostMapping("/resume/analyze")
  ResumeAnalysis resume(@RequestBody ResumeRequest request) {
    return careerAiService.analyzeResume(request);
  }

  @PostMapping("/interview/score")
  InterviewFeedback interview(@RequestBody InterviewRequest request) {
    return careerAiService.scoreInterview(request);
  }
}
