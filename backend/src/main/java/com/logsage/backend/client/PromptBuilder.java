package com.logsage.backend.client;

import com.logsage.backend.dto.LogEntry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Extracts prompt construction from the AI client.
 *
 * WHY: The system prompt was hardcoded inside AiClient, mixing HTTP concerns
 * with prompt engineering. Separated so prompts can evolve independently
 * of the API communication layer (OCP — open for extension, closed for modification).
 */
@Component
public class PromptBuilder {

    private static final String SYSTEM_PROMPT = """
            You are a DevOps expert.
            Analyze the log and return JSON with:
            1. error_type
            2. root_cause
            3. severity (LOW, MEDIUM, HIGH)
            4. fix_suggestion

            Return ONLY valid JSON. Do not include any markdown formatting, code blocks, or extra text.
            Example format:
            {"error_type": "...", "root_cause": "...", "severity": "...", "fix_suggestion": "..."}
            """;

    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * Format log entries into a human-readable string for the LLM prompt.
     */
    public String buildUserPrompt(List<LogEntry> logs) {
        StringBuilder sb = new StringBuilder("Analyze these logs:\n\n");
        for (LogEntry entry : logs) {
            sb.append(String.format("[%s] [%s] [%s] %s%n",
                    entry.getTimestamp(), entry.getService(),
                    entry.getLevel(), entry.getMessage()));
        }
        return sb.toString();
    }
}
