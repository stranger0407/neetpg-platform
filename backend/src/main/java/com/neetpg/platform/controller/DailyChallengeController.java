package com.neetpg.platform.controller;

import com.neetpg.platform.dto.DailyChallengeDto;
import com.neetpg.platform.security.UserPrincipal;
import com.neetpg.platform.service.DailyChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-challenge")
@RequiredArgsConstructor
public class DailyChallengeController {

    private final DailyChallengeService dailyChallengeService;

    @GetMapping("/today")
    public ResponseEntity<DailyChallengeDto.ChallengeInfo> getTodaysChallenge(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(dailyChallengeService.getTodaysChallenge(user.getId()));
    }

    @PostMapping("/start")
    public ResponseEntity<DailyChallengeDto.ChallengeInfo> startChallenge(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(dailyChallengeService.startChallenge(user.getId()));
    }

    @PostMapping("/submit")
    public ResponseEntity<DailyChallengeDto.ChallengeResult> submitChallenge(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody DailyChallengeDto.SubmitRequest request) {
        return ResponseEntity.ok(dailyChallengeService.submitChallenge(user.getId(), request));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<DailyChallengeDto.LeaderboardEntry>> getLeaderboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(dailyChallengeService.getLeaderboard(date));
    }
}
