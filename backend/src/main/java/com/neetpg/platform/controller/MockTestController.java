package com.neetpg.platform.controller;

import com.neetpg.platform.security.UserPrincipal;
import com.neetpg.platform.service.MockTestService;
import com.neetpg.platform.service.RevisionService;
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
}
