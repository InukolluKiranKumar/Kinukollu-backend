package com.kinukollu.backend.repository;

import com.kinukollu.backend.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long> {
    List<Case> findByUserIdOrderByCreatedAtDesc(Long userId);
}
