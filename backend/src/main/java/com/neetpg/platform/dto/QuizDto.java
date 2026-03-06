package com.neetpg.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class QuizDto {

    @Data
    public static class StartQuizRequest {
        private Long chapterId;
        private String quizType;
        private Integer questionCount;
        private String difficulty;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String difficulty;
        private String tags;
        private boolean bookmarked;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionWithAnswer {
        private Long id;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctAnswer;
        private String explanation;
        private String difficulty;
        private String selectedAnswer;
        @JsonProperty("isCorrect")
        private boolean isCorrect;
        private Integer timeTaken;
        private boolean bookmarked;
        private String userNote;
    }

    @Data
    public static class SubmitAnswerRequest {
        private Long questionId;
        private String selectedAnswer;
        private Integer timeTaken;
    }

    @Data
    public static class SubmitQuizRequest {
        private List<SubmitAnswerRequest> answers;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizResult {
        private Long sessionId;
        private int totalQuestions;
        private int correct;
        private int incorrect;
        private int skipped;
        private int marks;
        private double accuracy;
        private double averageTimeTaken;
        private List<QuestionWithAnswer> questionDetails;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizSessionResponse {
        private Long id;
        private String quizType;
        private String chapterName;
        private String subjectName;
        private int totalQuestions;
        private int correct;
        private int incorrect;
        private int skipped;
        private int marks;
        private String startedAt;
        private String completedAt;
    }
}
