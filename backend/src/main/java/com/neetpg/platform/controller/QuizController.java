package com.neetpg.platform.controller;

import com.neetpg.platform.dto.QuizDto;
import com.neetpg.platform.security.UserPrincipal;
import com.neetpg.platform.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startQuiz(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody QuizDto.StartQuizRequest request) {
        return ResponseEntity.ok(quizService.startQuiz(user.getId(), request));
    }

    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<QuizDto.QuizResult> submitQuiz(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId,
            @RequestBody QuizDto.SubmitQuizRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(user.getId(), sessionId, request));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<QuizDto.QuizResult> getResult(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(quizService.getQuizResult(user.getId(), sessionId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<QuizDto.QuizSessionResponse>> getSessions(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(quizService.getUserSessions(user.getId()));
    }
}
