package com.carrerai.repository;

import com.carrerai.model.CareerPlanEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerPlanRepository extends JpaRepository<CareerPlanEntity, Long> {
  List<CareerPlanEntity> findByUserEmailOrderByCreatedAtDesc(String userEmail);
  long count();

  @Query("SELECT AVG(c.readinessScore) FROM CareerPlanEntity c")
  Double averageReadinessScore();
}
