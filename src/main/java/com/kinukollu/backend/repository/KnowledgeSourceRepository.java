package com.kinukollu.backend.repository;

import com.kinukollu.backend.entity.KnowledgeSource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KnowledgeSourceRepository extends JpaRepository<KnowledgeSource, Long> {
    List<KnowledgeSource> findByCategory(String category);
    List<KnowledgeSource> findByCategoryAndApplicableStateIsNull(String category);
}
