package com.kinukollu.backend.dto;

import com.kinukollu.backend.entity.Case;
import java.time.LocalDateTime;

public class CaseResponse {

    private Long id;
    private String caseType;
    private String status;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CaseResponse(Case c) {
        this.id = c.getId();
        this.caseType = c.getCaseType();
        this.status = c.getStatus();
        this.summary = c.getSummary();
        this.createdAt = c.getCreatedAt();
        this.updatedAt = c.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getCaseType() { return caseType; }
    public String getStatus() { return status; }
    public String getSummary() { return summary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
