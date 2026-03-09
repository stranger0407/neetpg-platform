package com.neetpg.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class DailyChallengeDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionItem {
        private Long id;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String subject;
        private String chapter;
        private String difficulty;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChallengeInfo {
        private LocalDate date;
        private int questionCount;
        private int timeLimitMinutes;
        private boolean alreadyAttempted;
        private Long sessionId;
        private List<QuestionItem> questions;
        private ChallengeResult result;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AnswerSubmission {
        private Long questionId;
        private String selectedAnswer;
        private Integer timeTaken;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubmitRequest {
        private Long sessionId;
        private List<AnswerSubmission> answers;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChallengeResult {
        private int score;
        private int maxScore;
        private int correct;
        private int incorrect;
        private int skipped;
        private int totalQuestions;
        private double accuracy;
        private int rank;
        private int totalParticipants;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LeaderboardEntry {
        private int rank;
        private Long userId;
        private String userName;
        private int score;
        private int correct;
        private int totalQuestions;
    }
}
