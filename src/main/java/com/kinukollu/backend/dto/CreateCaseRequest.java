package com.kinukollu.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCaseRequest {

    @NotBlank
    private String caseType; // "RIGHTS_QUERY" or "SCHEME_MATCH"

    @NotBlank
    private String query; // the user's actual question/situation

    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
