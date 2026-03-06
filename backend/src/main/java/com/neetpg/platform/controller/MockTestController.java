package com.neetpg.platform.controller;

import com.neetpg.platform.security.UserPrincipal;
import com.neetpg.platform.service.MockTestService;
import com.neetpg.platform.service.RevisionService;
import com.neetpg.platform.service.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MockTestController {

    private final MockTestService mockTestService;
    private final RevisionService revisionService;
    private final SpacedRepetitionService spacedRepetitionService;

    @PostMapping("/mock-test/start")
    public ResponseEntity<Map<String, Object>> startMockTest(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(mockTestService.startMockTest(user.getId()));
    }

    @PostMapping("/revision/start")
    public ResponseEntity<Map<String, Object>> startRevision(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "50") int count) {
        return ResponseEntity.ok(revisionService.generateRevisionQuiz(user.getId(), count));
    }

    @PostMapping("/revision/bookmarked")
    public ResponseEntity<Map<String, Object>> bookmarkedQuiz(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(revisionService.getBookmarkedQuiz(user.getId()));
    }

    @GetMapping("/spaced-repetition/due-count")
    public ResponseEntity<Map<String, Object>> getDueCount(@AuthenticationPrincipal UserPrincipal user) {
        int count = spacedRepetitionService.getDueCount(user.getId());
        return ResponseEntity.ok(Map.of("dueCount", count));
    }

    @PostMapping("/spaced-repetition/due-quiz")
    public ResponseEntity<Map<String, Object>> startDueQuiz(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "20") int count) {
        return ResponseEntity.ok(revisionService.getDueReviewQuiz(user.getId(), count));
    }

    @PostMapping("/reattempt/incorrect/{sessionId}")
    public ResponseEntity<Map<String, Object>> reattemptIncorrect(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(revisionService.reattemptIncorrect(user.getId(), sessionId));
    }
}
