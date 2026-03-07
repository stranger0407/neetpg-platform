package com.neetpg.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neetpg.platform.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${groq.api-key:}")
    private String groqApiKey;

    private static final String[] GEMINI_MODELS = {
        "gemini-2.0-flash",
        "gemini-2.0-flash-lite",
        "gemini-1.5-flash"
    };

    private static final String[] GROQ_MODELS = {
        "llama-3.3-70b-versatile",
        "llama-3.1-8b-instant",
        "gemma2-9b-it"
    };

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isAvailable() {
        return (geminiApiKey != null && !geminiApiKey.isBlank())
            || (groqApiKey != null && !groqApiKey.isBlank());
    }

    private boolean isGeminiAvailable() {
        return geminiApiKey != null && !geminiApiKey.isBlank();
    }

    private boolean isGroqAvailable() {
        return groqApiKey != null && !groqApiKey.isBlank();
    }

    /**
     * Try Gemini first, then fall back to Groq.
     */
    private String callAiApi(String prompt) {
        // Try Gemini models first
        if (isGeminiAvailable()) {
            String result = callGeminiApi(prompt);
            if (result != null) return result;
        }

        // Fall back to Groq
        if (isGroqAvailable()) {
            log.info("Falling back to Groq API...");
            String result = callGroqApi(prompt);
            if (result != null) return result;
        }

        log.error("All AI providers failed");
        return null;
    }

    private String callGeminiApi(String prompt) {
        for (String model : GEMINI_MODELS) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + geminiApiKey;

                Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                    ))
                );

                String json = objectMapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    String text = root.path("candidates").path(0)
                            .path("content").path("parts").path(0)
                            .path("text").asText();
                    if (text != null && !text.isBlank()) {
                        log.info("Gemini responded successfully using model: {}", model);
                        return text;
                    }
                } else if (response.statusCode() == 429 || response.statusCode() == 403) {
                    log.warn("Gemini model {} quota/billing issue (status {}), trying next...", model, response.statusCode());
                } else {
                    log.warn("Gemini model {} returned status {}", model, response.statusCode());
                }
            } catch (Exception e) {
                log.error("Gemini API call failed for model {}", model, e);
            }
        }
        log.warn("All Gemini models exhausted or failed");
        return null;
    }

    private String callGroqApi(String prompt) {
        for (String model : GROQ_MODELS) {
            try {
                Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                    )),
                    "temperature", 0.7,
                    "max_tokens", 4096
                );

                String json = objectMapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + groqApiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    String text = root.path("choices").path(0)
                            .path("message").path("content").asText();
                    if (text != null && !text.isBlank()) {
                        log.info("Groq responded successfully using model: {}", model);
                        return text;
                    }
                } else if (response.statusCode() == 429) {
                    log.warn("Groq model {} rate limited, trying next...", model);
                } else {
                    log.warn("Groq model {} returned status {}: {}", model, response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.error("Groq API call failed for model {}", model, e);
            }
        }
        log.warn("All Groq models failed");
        return null;
    }

    public String generateExplanation(Question q) {
        if (!isAvailable()) {
            return null;
        }

        String correctOpt = switch (q.getCorrectAnswer()) {
            case "A" -> q.getOptionA();
            case "B" -> q.getOptionB();
            case "C" -> q.getOptionC();
            case "D" -> q.getOptionD();
            default -> "";
        };

        String existingExplanation = q.getExplanation() != null ? q.getExplanation() : "";

        String prompt = String.format(
            "You are an expert NEET PG medical exam tutor with deep clinical knowledge. " +
            "A student has already seen a brief general explanation and now wants a MUCH MORE DETAILED, " +
            "in-depth, point-by-point explanation. Do NOT repeat the general explanation verbatim.\n\n" +
            "Subject: %s | Chapter: %s | Difficulty: %s\n\n" +
            "Question: %s\n\n" +
            "A) %s\nB) %s\nC) %s\nD) %s\n\n" +
            "Correct Answer: %s) %s\n\n" +
            "General explanation already shown to student: %s\n\n" +
            "Now provide a COMPREHENSIVE and DETAILED explanation covering ALL of the following sections:\n\n" +
            "## 1. Detailed Topic Overview\n" +
            "- Explain the underlying medical concept/topic in depth\n" +
            "- Cover the relevant pathophysiology, mechanism of action, or clinical basis\n\n" +
            "## 2. Why Option %s is Correct\n" +
            "- Give a thorough, step-by-step reasoning for why this is the right answer\n" +
            "- Include relevant clinical correlations, lab findings, or pathological features\n\n" +
            "## 3. Why Each Other Option is Wrong\n" +
            "- **Option A) %s** - Explain specifically why this is incorrect and what condition/concept it actually relates to\n" +
            "- **Option B) %s** - Explain specifically why this is incorrect\n" +
            "- **Option C) %s** - Explain specifically why this is incorrect\n" +
            "- **Option D) %s** - Explain specifically why this is incorrect\n" +
            "(Skip the correct option from the list above)\n\n" +
            "## 4. High-Yield Points for NEET PG\n" +
            "- List 3-5 important points related to this topic that are frequently tested\n" +
            "- Include any classic presentations, pathognomonic findings, or diagnostic criteria\n\n" +
            "## 5. Memory Aid / Mnemonic\n" +
            "- Provide a mnemonic or easy way to remember the key concept\n\n" +
            "## 6. Related Clinical Scenario\n" +
            "- Give a brief clinical vignette where this concept would apply\n\n" +
            "Use markdown formatting with bold, bullet points, and headers. Be thorough and educational.",
            q.getChapter().getSubject().getName(),
            q.getChapter().getName(),
            q.getDifficulty().name(),
            q.getQuestionText(),
            q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
            q.getCorrectAnswer(), correctOpt,
            existingExplanation,
            q.getCorrectAnswer(),
            q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()
        );

        return callAiApi(prompt);
    }

    public String generateTutorResponse(Question q, String query) {
        if (!isAvailable()) {
            return null;
        }

        String prompt = String.format(
            "You are a NEET PG medical tutor. A student is asking about this question:\n\n" +
            "Subject: %s | Chapter: %s\n" +
            "Question: %s\n" +
            "A) %s  B) %s  C) %s  D) %s\n" +
            "Correct Answer: %s\n\n" +
            "Student's question: %s\n\n" +
            "Give a helpful, concise answer focused on the medical concept. Keep it under 200 words.",
            q.getChapter().getSubject().getName(),
            q.getChapter().getName(),
            q.getQuestionText(),
            q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
            q.getCorrectAnswer(),
            query
        );

        return callAiApi(prompt);
    }
}
