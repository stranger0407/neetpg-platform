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

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("## Correct Answer: Option %s — %s\n\n", q.getCorrectAnswer(), correctOpt));
        sb.append(String.format("**Subject:** %s | **Chapter:** %s | **Difficulty:** %s\n\n",
                q.getChapter().getSubject().getName(), q.getChapter().getName(), q.getDifficulty().name()));
        sb.append("---\n\n");
        sb.append("### Why this is correct\n");
        sb.append(String.format("Option %s (%s) is the correct answer. ", q.getCorrectAnswer(), correctOpt));
        if (q.getExplanation() != null && !q.getExplanation().isBlank()) {
            sb.append(q.getExplanation()).append("\n\n");
        } else {
            sb.append("Refer to standard textbooks for the detailed mechanism.\n\n");
        }
        sb.append("### Option-wise Analysis\n");
        String[] labels = {"A", "B", "C", "D"};
        String[] options = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        for (int i = 0; i < 4; i++) {
            if (labels[i].equals(q.getCorrectAnswer())) {
                sb.append(String.format("- **Option %s) %s** — ✅ Correct answer\n", labels[i], options[i]));
            } else {
                sb.append(String.format("- **Option %s) %s** — ❌ Incorrect. Review the topic of %s for differentiation.\n",
                        labels[i], options[i], q.getChapter().getName()));
            }
        }
        sb.append("\n### Key Takeaway\n");
        sb.append(String.format("Focus on the chapter **%s** under **%s** for deeper understanding of this concept.\n\n",
                q.getChapter().getName(), q.getChapter().getSubject().getName()));
        sb.append("\n> ⚠️ *AI-powered detailed explanation is currently unavailable. This is a structured breakdown based on available data. " +
                "Please try again later for a comprehensive AI explanation.*");
        return sb.toString();
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
