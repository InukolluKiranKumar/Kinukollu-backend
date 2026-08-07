package com.kinukollu.backend.controller;

import com.kinukollu.backend.dto.CreateKnowledgeRequest;
import com.kinukollu.backend.entity.KnowledgeSource;
import com.kinukollu.backend.repository.KnowledgeSourceRepository;
import com.kinukollu.backend.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeSourceRepository knowledgeSourceRepository;
    private final AiService aiService;

    public KnowledgeController(KnowledgeSourceRepository knowledgeSourceRepository, AiService aiService) {
        this.knowledgeSourceRepository = knowledgeSourceRepository;
        this.aiService = aiService;
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

        try {
            float[] embedding = aiService.generateEmbedding(request.getContent());
            entry.setEmbedding(aiService.embeddingToString(embedding));
        } catch (Exception e) {
            entry.setEmbedding(null);
        }

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

    @PostMapping("/backfill-embeddings")
    public ResponseEntity<?> backfillEmbeddings() {
        List<KnowledgeSource> all = knowledgeSourceRepository.findAll();
        int updated = 0;
        for (KnowledgeSource entry : all) {
            if (entry.getEmbedding() == null || entry.getEmbedding().isBlank()) {
                try {
                    float[] embedding = aiService.generateEmbedding(entry.getContent());
                    entry.setEmbedding(aiService.embeddingToString(embedding));
                    knowledgeSourceRepository.save(entry);
                    updated++;
                } catch (Exception e) {
                    // skip entries that fail, continue with the rest
                }
            }
        }
        return ResponseEntity.ok(Map.of("totalEntries", all.size(), "updated", updated));
    }
}
