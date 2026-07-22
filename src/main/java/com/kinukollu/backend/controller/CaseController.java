package com.kinukollu.backend.controller;

import com.kinukollu.backend.dto.CreateCaseRequest;
import com.kinukollu.backend.entity.Case;
import com.kinukollu.backend.entity.User;
import com.kinukollu.backend.repository.CaseRepository;
import com.kinukollu.backend.repository.UserRepository;
import com.kinukollu.backend.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    private static final String RIGHTS_SYSTEM_PROMPT = """
            You are a civic rights assistant for Indian citizens. Given a situation, explain:
            1. Which constitutional rights or laws may be relevant
            2. What the person can generally do next
            3. Which authority they could contact

            Keep it clear and practical. Always include a brief disclaimer that this is general
            information, not legal advice, and that a lawyer should be consulted for serious matters.
            """;

    public CaseController(CaseRepository caseRepository, UserRepository userRepository, AiService aiService) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<?> createCase(@Valid @RequestBody CreateCaseRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Case newCase = new Case();
        newCase.setUser(user);
        newCase.setCaseType(request.getCaseType());
        newCase.setStatus("OPEN");
        newCase.setSummary(request.getQuery());

        Case saved = caseRepository.save(newCase);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<?> getMyCases(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Case> cases = caseRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(cases);
    }

    @PostMapping("/{id}/ask")
    public ResponseEntity<?> askAboutCase(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Case existingCase = caseRepository.findById(id).orElse(null);
        if (existingCase == null || !existingCase.getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        String answer = aiService.askClaude(RIGHTS_SYSTEM_PROMPT, existingCase.getSummary());

        return ResponseEntity.ok(Map.of(
                "caseId", existingCase.getId(),
                "query", existingCase.getSummary(),
                "answer", answer
        ));
    }
}
