package com.kinukollu.backend.controller;

import com.kinukollu.backend.dto.CaseResponse;
import com.kinukollu.backend.dto.CreateCaseRequest;
import com.kinukollu.backend.entity.Case;
import com.kinukollu.backend.entity.KnowledgeSource;
import com.kinukollu.backend.entity.User;
import com.kinukollu.backend.repository.CaseRepository;
import com.kinukollu.backend.repository.KnowledgeSourceRepository;
import com.kinukollu.backend.repository.UserRepository;
import com.kinukollu.backend.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final KnowledgeSourceRepository knowledgeSourceRepository;
    private final AiService aiService;

    private static final String RIGHTS_SYSTEM_PROMPT = """
            You are a civic rights assistant for Indian citizens. Base your answer on the
            reference material provided below, applying it thoughtfully rather than requiring
            an exact keyword match.

            IMPORTANT DISTINCTION:
            - Constitutional RIGHTS (e.g. Article 21, Article 22) are usually BROADLY applicable.
              If a provided right's protections are relevant to the user's situation — even if
              indirectly, such as personal dignity, safety, or liberty being affected — you
              SHOULD apply and explain it. Do not withhold clearly relevant rights just because
              the wording is not an exact match.
            - SCHEMES (e.g. specific government benefit programs) are usually NARROWLY applicable
              based on strict eligibility criteria. Only present a scheme as relevant if the
              user's situation genuinely appears to meet its eligibility criteria. If no scheme
              in the reference material fits, say so explicitly rather than forcing a fit.

            Do not invent rights, laws, or schemes that are not in the reference material below.

            Respond in this exact structure using short bullet points (each bullet 1-2 sentences
            max, no long paragraphs):

            **Relevant Rights/Laws**
            - bullet points here, or "No specific match found in our current records" only if
              truly nothing in the reference material applies

            **What You Can Do**
            - bullet points here

            **Authorities to Contact**
            - bullet points here

            **Disclaimer**
            - one short line noting this is general information, not legal advice, and a lawyer
              should be consulted for serious matters

            Do not write dense paragraphs. Every point should be its own short bullet line.
            """;

    public CaseController(CaseRepository caseRepository, UserRepository userRepository,
                           KnowledgeSourceRepository knowledgeSourceRepository, AiService aiService) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.knowledgeSourceRepository = knowledgeSourceRepository;
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
        return ResponseEntity.ok(new CaseResponse(saved));
    }

    @GetMapping
    public ResponseEntity<?> getMyCases(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<CaseResponse> cases = caseRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(CaseResponse::new)
                .collect(Collectors.toList());
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

        String category = existingCase.getCaseType().equals("SCHEME_MATCH") ? "SCHEME" : "RIGHTS";
        List<KnowledgeSource> allInCategory = knowledgeSourceRepository.findByCategory(category);

        List<KnowledgeSource> relevantKnowledge;
        try {
            float[] queryEmbedding = aiService.generateEmbedding(existingCase.getSummary());
            relevantKnowledge = allInCategory.stream()
                    .sorted((a, b) -> {
                        double simA = aiService.cosineSimilarity(queryEmbedding, aiService.stringToEmbedding(a.getEmbedding()));
                        double simB = aiService.cosineSimilarity(queryEmbedding, aiService.stringToEmbedding(b.getEmbedding()));
                        return Double.compare(simB, simA);
                    })
                    .limit(5)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            relevantKnowledge = allInCategory;
        }

        String knowledgeContext = relevantKnowledge.stream()
                .map(k -> "- " + k.getTitle() + ": " + k.getContent() +
                        (k.getEligibilityCriteria() != null ? " (Eligibility: " + k.getEligibilityCriteria() + ")" : ""))
                .collect(Collectors.joining("\n"));

        String fullPrompt = "Reference material:\n" + knowledgeContext +
                "\n\nUser's situation: " + existingCase.getSummary();

        String answer = aiService.askClaude(RIGHTS_SYSTEM_PROMPT, fullPrompt);

        return ResponseEntity.ok(Map.of(
                "caseId", existingCase.getId(),
                "query", existingCase.getSummary(),
                "knowledgeUsed", relevantKnowledge.size(),
                "answer", answer
        ));
    }
}
