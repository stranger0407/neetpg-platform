package com.neetpg.platform.service;

import com.neetpg.platform.dto.AnalyticsDto;
import com.neetpg.platform.entity.User;
import com.neetpg.platform.exception.BadRequestException;
import com.neetpg.platform.repository.QuizSessionRepository;
import com.neetpg.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        // Batch-load all users to avoid N+1
        List<Long> userIds = raw.stream().map(row -> (Long) row[0]).limit(100).collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<AnalyticsDto.LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Object[] row : raw) {
            Long userId = (Long) row[0];
            long marks = (Long) row[1];
            User user = userMap.get(userId);
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

        // Verify session belongs to the requesting user
        if (!session.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to quiz session");
        }

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
        // Batch-load all users to avoid N+1
        List<Long> userIds = raw.stream().map(row -> (Long) row[0]).limit(100).collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<AnalyticsDto.LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Object[] row : raw) {
            Long userId = (Long) row[0];
            long marks = (Long) row[1];
            long correct = (Long) row[2];
            User user = userMap.get(userId);
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
