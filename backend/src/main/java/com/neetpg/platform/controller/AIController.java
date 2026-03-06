package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Question;
import com.neetpg.platform.exception.ResourceNotFoundException;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.security.UserPrincipal;
import com.neetpg.platform.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final QuestionRepository questionRepository;
    private final GeminiService geminiService;

    @GetMapping("/explain/{questionId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getExplanation(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long questionId) {
        Question q = questionRepository.findByIdWithChapterAndSubject(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        String aiExplanation = geminiService.generateExplanation(q);

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", q.getId());
        result.put("explanation", q.getExplanation());
        result.put("detailedExplanation", aiExplanation != null ? aiExplanation : generateFallbackExplanation(q));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tutor")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> askTutor(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, Object> request) {
        Long questionId = Long.valueOf(request.get("questionId").toString());
        String query = (String) request.get("query");

        Question q = questionRepository.findByIdWithChapterAndSubject(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        String aiResponse = geminiService.generateTutorResponse(q, query);

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", q.getId());
        result.put("answer", aiResponse != null ? aiResponse : generateFallbackTutor(q, query));
        return ResponseEntity.ok(result);
    }

    private String generateFallbackExplanation(Question q) {
        String correctOpt = switch (q.getCorrectAnswer()) {
            case "A" -> q.getOptionA();
            case "B" -> q.getOptionB();
            case "C" -> q.getOptionC();
            case "D" -> q.getOptionD();
            default -> "";
        };

        return String.format(
            "**Correct Answer: Option %s** - %s\n\n" +
            "**Explanation:** %s\n\n" +
            "**Subject:** %s | **Chapter:** %s | **Difficulty:** %s",
            q.getCorrectAnswer(), correctOpt,
            q.getExplanation() != null ? q.getExplanation() : "Refer to standard textbooks for detailed explanation.",
            q.getChapter().getSubject().getName(), q.getChapter().getName(),
            q.getDifficulty().name()
        );
    }

    private String generateFallbackTutor(Question q, String query) {
        return String.format(
            "Regarding this question about %s (%s):\n\n" +
            "The correct answer is Option %s. %s\n\n" +
            "For more in-depth study, refer to the standard textbook chapter on %s.",
            q.getChapter().getSubject().getName(), q.getChapter().getName(),
            q.getCorrectAnswer(),
            q.getExplanation() != null ? q.getExplanation() : "",
            q.getChapter().getName()
        );
    }
}
