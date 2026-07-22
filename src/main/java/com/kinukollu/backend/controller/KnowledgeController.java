package com.kinukollu.backend.controller;

import com.kinukollu.backend.dto.CreateKnowledgeRequest;
import com.kinukollu.backend.entity.KnowledgeSource;
import com.kinukollu.backend.repository.KnowledgeSourceRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeSourceRepository knowledgeSourceRepository;

    public KnowledgeController(KnowledgeSourceRepository knowledgeSourceRepository) {
        this.knowledgeSourceRepository = knowledgeSourceRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateKnowledgeRequest request) {
        KnowledgeSource entry = new KnowledgeSource();
        entry.setCategory(request.getCategory());
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setApplicableState(request.getApplicableState());
        entry.setEligibilityCriteria(request.getEligibilityCriteria());
        entry.setSourceReference(request.getSourceReference());

        KnowledgeSource saved = knowledgeSourceRepository.save(entry);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<KnowledgeSource> all = knowledgeSourceRepository.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getByCategory(@PathVariable String category) {
        List<KnowledgeSource> results = knowledgeSourceRepository.findByCategory(category);
        return ResponseEntity.ok(results);
    }
}
