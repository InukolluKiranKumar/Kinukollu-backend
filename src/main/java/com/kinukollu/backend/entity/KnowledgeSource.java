package com.kinukollu.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_sources")
public class KnowledgeSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // e.g. "RIGHTS", "SCHEME", "PROCEDURE"

    @Column(nullable = false)
    private String title; // e.g. "Article 21 - Right to Life", "PM Kisan Samman Nidhi"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // the curated explanation/rule text

    private String applicableState; // null = all-India, else state-specific

    @Column(columnDefinition = "TEXT")
    private String eligibilityCriteria; // for schemes: structured or plain-text rules

    private String sourceReference; // e.g. official gov URL or legal citation

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public KnowledgeSource() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getApplicableState() { return applicableState; }
    public void setApplicableState(String applicableState) { this.applicableState = applicableState; }

    public String getEligibilityCriteria() { return eligibilityCriteria; }
    public void setEligibilityCriteria(String eligibilityCriteria) { this.eligibilityCriteria = eligibilityCriteria; }

    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
