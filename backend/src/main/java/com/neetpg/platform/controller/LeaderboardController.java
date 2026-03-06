package com.neetpg.platform.controller;

import com.neetpg.platform.dto.AnalyticsDto;
import com.neetpg.platform.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.neetpg.platform.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/weekly")
    public ResponseEntity<List<AnalyticsDto.LeaderboardEntry>> getWeeklyLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboard());
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<AnalyticsDto.LeaderboardEntry>> getSubjectLeaderboard(
            @PathVariable Long subjectId) {
        return ResponseEntity.ok(leaderboardService.getSubjectLeaderboard(subjectId));
    }

    @GetMapping("/mock-result/{sessionId}")
    public ResponseEntity<AnalyticsDto.MockTestResult> getMockTestResult(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(leaderboardService.calculateMockTestResult(
                user.getId(), sessionId, 100000));
    }
}
