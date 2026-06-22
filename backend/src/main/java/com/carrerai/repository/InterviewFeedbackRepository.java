package com.carrerai.repository;

import com.carrerai.model.InterviewFeedbackEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedbackEntity, Long> {
  List<InterviewFeedbackEntity> findByUserEmailOrderByCreatedAtDesc(String userEmail);
  long count();

  @Query("SELECT AVG(i.score) FROM InterviewFeedbackEntity i")
  Double averageScore();
}
