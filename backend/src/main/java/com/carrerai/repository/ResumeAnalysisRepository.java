package com.carrerai.repository;

import com.carrerai.model.ResumeAnalysisEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysisEntity, Long> {
  List<ResumeAnalysisEntity> findByUserEmailOrderByCreatedAtDesc(String userEmail);
  long count();
}
