package com.carrerai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysisEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userEmail;

  private String targetRole;

  @Column(columnDefinition = "TEXT")
  private String resumeText;

  private int atsScore;
  private int contentScore;
  private int impactScore;

  @Column(columnDefinition = "TEXT")
  private String rawResponse;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public ResumeAnalysisEntity() {}

  public ResumeAnalysisEntity(String userEmail, String targetRole, String resumeText,
      int atsScore, int contentScore, int impactScore, String rawResponse) {
    this.userEmail = userEmail;
    this.targetRole = targetRole;
    this.resumeText = resumeText;
    this.atsScore = atsScore;
    this.contentScore = contentScore;
    this.impactScore = impactScore;
    this.rawResponse = rawResponse;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getUserEmail() { return userEmail; }
  public String getTargetRole() { return targetRole; }
  public String getResumeText() { return resumeText; }
  public int getAtsScore() { return atsScore; }
  public int getContentScore() { return contentScore; }
  public int getImpactScore() { return impactScore; }
  public String getRawResponse() { return rawResponse; }
  public Instant getCreatedAt() { return createdAt; }
}
