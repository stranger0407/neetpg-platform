package com.neetpg.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AnalyticsDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OverallStats {
        private long totalAttempted;
        private long correctAnswers;
        private long incorrectAnswers;
        private long skippedAnswers;
        private int totalMarks;
        private double accuracy;
        private double averageTimeTaken;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChapterStat {
        private Long chapterId;
        private String chapterName;
        private String subjectName;
        private long totalAttempted;
        private long correct;
        private double accuracy;
        private String strength;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubjectStat {
        private Long subjectId;
        private String subjectName;
        private long totalAttempted;
        private long correct;
        private double accuracy;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DailyStat {
        private String date;
        private long totalAttempted;
        private long correct;
        private double accuracy;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DashboardData {
        private OverallStats overallStats;
        private List<SubjectStat> subjectStats;
        private List<ChapterStat> weakTopics;
        private List<ChapterStat> strongTopics;
        private List<DailyStat> dailyProgress;
        private int questionsForReview;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LeaderboardEntry {
        private int rank;
        private Long userId;
        private String userName;
        private long totalMarks;
        private long totalCorrect;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MockTestResult {
        private int score;
        private double percentile;
        private int predictedRank;
        private int totalQuestions;
        private int correct;
        private int incorrect;
        private int skipped;
    }
}
