package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Question;
import com.neetpg.platform.exception.ResourceNotFoundException;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.security.UserPrincipal;
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

    @GetMapping("/explain/{questionId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getExplanation(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long questionId) {
        Question q = questionRepository.findByIdWithChapterAndSubject(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", q.getId());
        result.put("explanation", q.getExplanation());
        result.put("detailedExplanation", generateDetailedExplanation(q));
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

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", q.getId());
        result.put("answer", generateTutorResponse(q, query));
        return ResponseEntity.ok(result);
    }

    private String generateDetailedExplanation(Question q) {
        String correctOpt = switch (q.getCorrectAnswer()) {
            case "A" -> q.getOptionA();
            case "B" -> q.getOptionB();
            case "C" -> q.getOptionC();
            case "D" -> q.getOptionD();
            default -> "";
        };

        return String.format(
            "## Detailed Explanation\n\n" +
            "**Question:** %s\n\n" +
            "**Correct Answer: Option %s** - %s\n\n" +
            "### Why this is correct:\n" +
            "%s\n\n" +
            "### Why other options are incorrect:\n" +
            "- **Option A** (%s): %s\n" +
            "- **Option B** (%s): %s\n" +
            "- **Option C** (%s): %s\n" +
            "- **Option D** (%s): %s\n\n" +
            "### Key Concepts:\n" +
            "This question tests knowledge in the area of %s (%s). " +
            "Understanding the fundamental principles is essential for NEET PG preparation. " +
            "Review the relevant chapter for deeper understanding.\n\n" +
            "**Difficulty:** %s | **Source:** %s",
            q.getQuestionText(),
            q.getCorrectAnswer(), correctOpt,
            q.getExplanation() != null ? q.getExplanation() : "Refer to standard textbooks for detailed explanation.",
            q.getOptionA(), q.getCorrectAnswer().equals("A") ? "This is the correct answer." : "This option does not accurately describe the concept being tested.",
            q.getOptionB(), q.getCorrectAnswer().equals("B") ? "This is the correct answer." : "This option contains a factual inaccuracy or does not best answer the question.",
            q.getOptionC(), q.getCorrectAnswer().equals("C") ? "This is the correct answer." : "While partially related, this option is not the most accurate answer.",
            q.getOptionD(), q.getCorrectAnswer().equals("D") ? "This is the correct answer." : "This option describes a different concept or condition.",
            q.getChapter().getSubject().getName(), q.getChapter().getName(),
            q.getDifficulty().name(),
            q.getSource() != null ? q.getSource() : "Standard textbook"
        );
    }

    private String generateTutorResponse(Question q, String query) {
        String lowerQuery = query != null ? query.toLowerCase() : "";

        if (lowerQuery.contains("why") && lowerQuery.contains("correct")) {
            String correctOpt = switch (q.getCorrectAnswer()) {
                case "A" -> q.getOptionA();
                case "B" -> q.getOptionB();
                case "C" -> q.getOptionC();
                case "D" -> q.getOptionD();
                default -> "";
            };
            return String.format(
                "Option %s (%s) is correct because: %s\n\n" +
                "This is a well-established concept in %s, specifically in the chapter on %s. " +
                "The key distinguishing factor is that this option most accurately and completely describes the concept being tested.",
                q.getCorrectAnswer(), correctOpt,
                q.getExplanation() != null ? q.getExplanation() : "This is the most accurate option based on standard medical knowledge.",
                q.getChapter().getSubject().getName(), q.getChapter().getName()
            );
        }

        if (lowerQuery.contains("why") && lowerQuery.contains("wrong")) {
            return String.format(
                "The other options are incorrect because:\n\n" +
                "- **Option A** (%s): %s\n" +
                "- **Option B** (%s): %s\n" +
                "- **Option C** (%s): %s\n" +
                "- **Option D** (%s): %s\n\n" +
                "The correct answer is Option %s. Always look for the most specific and accurate option.",
                q.getOptionA(), q.getCorrectAnswer().equals("A") ? "CORRECT" : "Does not fully address the concept or contains inaccuracies.",
                q.getOptionB(), q.getCorrectAnswer().equals("B") ? "CORRECT" : "May be a common distractor but is not the best answer.",
                q.getOptionC(), q.getCorrectAnswer().equals("C") ? "CORRECT" : "Partially related but incomplete or inaccurate.",
                q.getOptionD(), q.getCorrectAnswer().equals("D") ? "CORRECT" : "Describes a different scenario or condition.",
                q.getCorrectAnswer()
            );
        }

        return String.format(
            "Regarding this question about %s (%s):\n\n" +
            "%s\n\n" +
            "The correct answer is Option %s. %s\n\n" +
            "For more in-depth study, refer to the standard textbook chapter on %s.",
            q.getChapter().getSubject().getName(), q.getChapter().getName(),
            q.getQuestionText(),
            q.getCorrectAnswer(),
            q.getExplanation() != null ? q.getExplanation() : "",
            q.getChapter().getName()
        );
    }
}
