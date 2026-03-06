package com.neetpg.platform.service;

import com.neetpg.platform.dto.AnalyticsDto;
import com.neetpg.platform.entity.User;
import com.neetpg.platform.repository.QuizSessionRepository;
import com.neetpg.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final QuizSessionRepository quizSessionRepository;
    private final UserRepository userRepository;

    public List<AnalyticsDto.LeaderboardEntry> getWeeklyLeaderboard() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return buildLeaderboard(quizSessionRepository.getLeaderboard(since));
    }

    public List<AnalyticsDto.LeaderboardEntry> getSubjectLeaderboard(Long subjectId) {
        List<Object[]> raw = quizSessionRepository.getSubjectLeaderboard(subjectId);
        List<AnalyticsDto.LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Object[] row : raw) {
            Long userId = (Long) row[0];
            long marks = (Long) row[1];
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            entries.add(AnalyticsDto.LeaderboardEntry.builder()
                    .rank(rank++)
                    .userId(userId)
                    .userName(user.getName())
                    .totalMarks(marks)
                    .build());
            if (rank > 100) break;
        }
        return entries;
    }

    public AnalyticsDto.MockTestResult calculateMockTestResult(Long userId, Long sessionId, int totalParticipants) {
        var session = quizSessionRepository.findById(sessionId).orElseThrow();

        int score = session.getMarks();
        int maxScore = session.getTotalQuestions() * 4;

        double scorePercent = maxScore > 0 ? (double) score / maxScore * 100 : 0;
        double percentile = Math.min(99.9, Math.max(0, scorePercent * 0.95));
        int predictedRank = Math.max(1, (int) ((100 - percentile) / 100.0 * totalParticipants));

        return AnalyticsDto.MockTestResult.builder()
                .score(score)
                .percentile(Math.round(percentile * 100.0) / 100.0)
                .predictedRank(predictedRank)
                .totalQuestions(session.getTotalQuestions())
                .correct(session.getCorrect())
                .incorrect(session.getIncorrect())
                .skipped(session.getSkipped())
                .build();
    }

    private List<AnalyticsDto.LeaderboardEntry> buildLeaderboard(List<Object[]> raw) {
        List<AnalyticsDto.LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Object[] row : raw) {
            Long userId = (Long) row[0];
            long marks = (Long) row[1];
            long correct = (Long) row[2];
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            entries.add(AnalyticsDto.LeaderboardEntry.builder()
                    .rank(rank++)
                    .userId(userId)
                    .userName(user.getName())
                    .totalMarks(marks)
                    .totalCorrect(correct)
                    .build());
            if (rank > 100) break;
        }
        return entries;
    }
}
