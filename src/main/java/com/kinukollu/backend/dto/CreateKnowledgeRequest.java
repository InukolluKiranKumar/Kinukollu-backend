package com.kinukollu.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateKnowledgeRequest {

    @NotBlank
    private String category;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String applicableState;
    private String eligibilityCriteria;
    private String sourceReference;

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
}
