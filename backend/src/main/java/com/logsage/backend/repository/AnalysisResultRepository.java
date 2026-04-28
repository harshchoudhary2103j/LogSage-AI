package com.logsage.backend.repository;

import com.logsage.backend.entity.AnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for AI analysis results.
 * Replaces AnalysisResultStore.
 */
public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, Long> {

    List<AnalysisResultEntity> findByService(String service);

    List<AnalysisResultEntity> findAllByOrderByAnalyzedAtDesc();

    List<AnalysisResultEntity> findByServiceOrderByAnalyzedAtDesc(String service);
}
