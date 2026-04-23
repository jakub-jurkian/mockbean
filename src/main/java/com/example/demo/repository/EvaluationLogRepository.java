package com.example.demo.repository;

import com.example.demo.domain.EvaluationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationLogRepository extends JpaRepository<EvaluationLog, Long> {
    // Spring injects here the whole db magic automatically
}