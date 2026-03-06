package com.neetpg.platform.controller;

import com.neetpg.platform.dto.AnalyticsDto;
import com.neetpg.platform.security.UserPrincipal;
import com.neetpg.platform.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDto.DashboardData> getDashboard(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(analyticsService.getDashboard(user.getId()));
    }

    @GetMapping("/overall")
    public ResponseEntity<AnalyticsDto.OverallStats> getOverallStats(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(analyticsService.getOverallStats(user.getId()));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<AnalyticsDto.SubjectStat>> getSubjectStats(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(analyticsService.getSubjectStats(user.getId()));
    }

    @GetMapping("/chapters")
    public ResponseEntity<List<AnalyticsDto.ChapterStat>> getChapterStats(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(analyticsService.getChapterStats(user.getId()));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<AnalyticsDto.DailyStat>> getDailyProgress(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getDailyProgress(user.getId(), days));
    }
}
