package com.neetpg.platform.service;

import com.neetpg.platform.dto.AnalyticsDto;
import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AttemptRepository attemptRepository;
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    public AnalyticsDto.OverallStats getOverallStats(Long userId) {
        long total = attemptRepository.countByUserId(userId);
        long correct = attemptRepository.countByUserIdAndIsCorrect(userId, true);
        long incorrect = attemptRepository.countByUserIdAndIsCorrect(userId, false);
        Double avgTime = attemptRepository.getAverageTimeTaken(userId);

        // Note: skipped questions have isCorrect=false and null/blank selectedAnswer.
        // Since we can't distinguish them at DB level, skipped is always 0 in overall stats.
        // Accurate skipped counts are stored per-session in QuizSession entity.
        double accuracy = (correct + incorrect) > 0 ? (correct * 100.0) / (correct + incorrect) : 0;
        int marks = (int) ((correct * 4) - (incorrect * 1));

        return AnalyticsDto.OverallStats.builder()
                .totalAttempted(total)
                .correctAnswers(correct)
                .incorrectAnswers(incorrect)
                .skippedAnswers(total - correct - incorrect)
                .totalMarks(marks)
                .accuracy(Math.round(accuracy * 100.0) / 100.0)
                .averageTimeTaken(avgTime != null ? Math.round(avgTime * 100.0) / 100.0 : 0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDto.ChapterStat> getChapterStats(Long userId) {
        List<Object[]> stats = attemptRepository.getChapterWiseStats(userId);
        List<AnalyticsDto.ChapterStat> result = new ArrayList<>();

        // Batch-load all chapters to avoid N+1
        List<Long> chapterIds = stats.stream().map(row -> (Long) row[0]).collect(Collectors.toList());
        Map<Long, Chapter> chapterMap = chapterRepository.findAllById(chapterIds).stream()
                .collect(Collectors.toMap(Chapter::getId, Function.identity()));

        for (Object[] row : stats) {
            Long chapterId = (Long) row[0];
            long total = (Long) row[1];
            long correct = (Long) row[2];
            double accuracy = total > 0 ? (correct * 100.0) / total : 0;

            Chapter chapter = chapterMap.get(chapterId);
            if (chapter == null) continue;

            String strength;
            if (accuracy > 70) strength = "STRONG";
            else if (accuracy >= 40) strength = "AVERAGE";
            else strength = "WEAK";

            result.add(AnalyticsDto.ChapterStat.builder()
                    .chapterId(chapterId)
                    .chapterName(chapter.getName())
                    .subjectName(chapter.getSubject().getName())
                    .totalAttempted(total)
                    .correct(correct)
                    .accuracy(Math.round(accuracy * 100.0) / 100.0)
                    .strength(strength)
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDto.SubjectStat> getSubjectStats(Long userId) {
        List<Object[]> stats = attemptRepository.getSubjectWiseStats(userId);
        List<AnalyticsDto.SubjectStat> result = new ArrayList<>();

        // Batch-load all subjects to avoid N+1
        List<Long> subjectIds = stats.stream().map(row -> (Long) row[0]).collect(Collectors.toList());
        Map<Long, Subject> subjectMap = subjectRepository.findAllById(subjectIds).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        for (Object[] row : stats) {
            Long subjectId = (Long) row[0];
            long total = (Long) row[1];
            long correct = (Long) row[2];
            double accuracy = total > 0 ? (correct * 100.0) / total : 0;

            Subject subject = subjectMap.get(subjectId);
            if (subject == null) continue;

            result.add(AnalyticsDto.SubjectStat.builder()
                    .subjectId(subjectId)
                    .subjectName(subject.getName())
                    .totalAttempted(total)
                    .correct(correct)
                    .accuracy(Math.round(accuracy * 100.0) / 100.0)
                    .build());
        }
        return result;
    }

    public List<AnalyticsDto.DailyStat> getDailyProgress(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> stats = attemptRepository.getDailyStats(userId, since);
        List<AnalyticsDto.DailyStat> result = new ArrayList<>();

        for (Object[] row : stats) {
            String date = row[0].toString();
            long total = (Long) row[1];
            long correct = (Long) row[2];
            double accuracy = total > 0 ? (correct * 100.0) / total : 0;

            result.add(AnalyticsDto.DailyStat.builder()
                    .date(date)
                    .totalAttempted(total)
                    .correct(correct)
                    .accuracy(Math.round(accuracy * 100.0) / 100.0)
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AnalyticsDto.DashboardData getDashboard(Long userId) {
        AnalyticsDto.OverallStats overall = getOverallStats(userId);
        List<AnalyticsDto.SubjectStat> subjectStats = getSubjectStats(userId);
        List<AnalyticsDto.ChapterStat> chapterStats = getChapterStats(userId);
        List<AnalyticsDto.DailyStat> daily = getDailyProgress(userId, 30);

        List<AnalyticsDto.ChapterStat> weak = chapterStats.stream()
                .filter(c -> "WEAK".equals(c.getStrength()))
                .toList();
        List<AnalyticsDto.ChapterStat> strong = chapterStats.stream()
                .filter(c -> "STRONG".equals(c.getStrength()))
                .toList();

        int reviewCount = spacedRepetitionService.getDueCount(userId);

        return AnalyticsDto.DashboardData.builder()
                .overallStats(overall)
                .subjectStats(subjectStats)
                .weakTopics(weak)
                .strongTopics(strong)
                .dailyProgress(daily)
                .questionsForReview(reviewCount)
                .build();
    }
}
