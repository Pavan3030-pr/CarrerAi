package com.carrerai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "career_plans")
public class CareerPlanEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userEmail;

  @Column(nullable = false)
  private String name;

  private String degree;
  private String targetRole;
  private String experienceLevel;

  private int readinessScore;

  @Column(columnDefinition = "TEXT")
  private String rawResponse;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public CareerPlanEntity() {}

  public CareerPlanEntity(String userEmail, String name, String degree,
      String targetRole, String experienceLevel, int readinessScore, String rawResponse) {
    this.userEmail = userEmail;
    this.name = name;
    this.degree = degree;
    this.targetRole = targetRole;
    this.experienceLevel = experienceLevel;
    this.readinessScore = readinessScore;
    this.rawResponse = rawResponse;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getUserEmail() { return userEmail; }
  public String getName() { return name; }
  public String getDegree() { return degree; }
  public String getTargetRole() { return targetRole; }
  public String getExperienceLevel() { return experienceLevel; }
  public int getReadinessScore() { return readinessScore; }
  public String getRawResponse() { return rawResponse; }
  public Instant getCreatedAt() { return createdAt; }
}
