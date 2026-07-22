package com.kinukollu.backend.service;

import org.springframework.stereotype.Service;

@Service
public class AiService {

    // TODO: Replace this mock with a real API call once billing/provider is set up.
    // Swap the internals of this method only — no other code needs to change.
    public String askClaude(String systemPrompt, String userMessage) {
        return "[MOCK RESPONSE] Based on your situation: \"" + userMessage + "\"\n\n"
                + "This is placeholder text standing in for a real AI-generated answer. "
                + "Once a live API key with credits is connected, this will be replaced "
                + "with an actual rights/scheme explanation tailored to your query.\n\n"
                + "Disclaimer: This is general information, not legal advice.";
    }
}
