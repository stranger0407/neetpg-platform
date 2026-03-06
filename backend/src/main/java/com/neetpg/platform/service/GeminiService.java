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
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
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

        String prompt = String.format(
            "You are a NEET PG medical exam tutor. Explain this MCQ in detail for a medical student.\n\n" +
            "Subject: %s | Chapter: %s | Difficulty: %s\n\n" +
            "Question: %s\n\n" +
            "A) %s\nB) %s\nC) %s\nD) %s\n\n" +
            "Correct Answer: %s) %s\n\n" +
            "Provide a structured explanation with:\n" +
            "1. **Why the correct answer is right** - explain the medical concept\n" +
            "2. **Why each wrong option is incorrect** - be specific for each option\n" +
            "3. **Key concept to remember** - a concise takeaway for revision\n\n" +
            "Keep it concise but informative. Use simple language suitable for NEET PG preparation.",
            q.getChapter().getSubject().getName(),
            q.getChapter().getName(),
            q.getDifficulty().name(),
            q.getQuestionText(),
            q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
            q.getCorrectAnswer(), correctOpt
        );

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

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
                return root.path("candidates").path(0)
                        .path("content").path("parts").path(0)
                        .path("text").asText();
            } else {
                log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            return null;
        }
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

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

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
                return root.path("candidates").path(0)
                        .path("content").path("parts").path(0)
                        .path("text").asText();
            } else {
                log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            return null;
        }
    }
}
