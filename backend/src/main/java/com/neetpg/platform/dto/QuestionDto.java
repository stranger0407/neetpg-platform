package com.neetpg.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class QuestionDto {

    @Data
    public static class CreateRequest {
        private Long chapterId;
        @NotBlank
        private String questionText;
        @NotBlank
        private String optionA;
        @NotBlank
        private String optionB;
        @NotBlank
        private String optionC;
        @NotBlank
        private String optionD;
        @NotBlank
        private String correctAnswer;
        private String explanation;
        private String difficulty;
        private String source;
        private String tags;
        private boolean previousYear;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AdminQuestionResponse {
        private Long id;
        private Long chapterId;
        private String chapterName;
        private String subjectName;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctAnswer;
        private String explanation;
        private String difficulty;
        private String source;
        private String tags;
        private boolean previousYear;
    }
}
