package com.carrerai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @JsonIgnore
  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  public UserEntity() {}

  public UserEntity(String name, String email, String passwordHash) {
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createdAt = Instant.now();
  }

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getEmail() { return email; }

  @JsonIgnore
  public String getPasswordHash() { return passwordHash; }

  public Instant getCreatedAt() { return createdAt; }
}
