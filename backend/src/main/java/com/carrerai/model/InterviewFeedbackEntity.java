package com.carrerai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "interview_feedbacks")
public class InterviewFeedbackEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userEmail;

  private String targetRole;
  private String question;

  @Column(columnDefinition = "TEXT")
  private String answer;

  private int score;

  @Column(columnDefinition = "TEXT")
  private String rawResponse;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public InterviewFeedbackEntity() {}

  public InterviewFeedbackEntity(String userEmail, String targetRole, String question,
      String answer, int score, String rawResponse) {
    this.userEmail = userEmail;
    this.targetRole = targetRole;
    this.question = question;
    this.answer = answer;
    this.score = score;
    this.rawResponse = rawResponse;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getUserEmail() { return userEmail; }
  public String getTargetRole() { return targetRole; }
  public String getQuestion() { return question; }
  public String getAnswer() { return answer; }
  public int getScore() { return score; }
  public String getRawResponse() { return rawResponse; }
  public Instant getCreatedAt() { return createdAt; }
}
