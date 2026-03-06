package com.neetpg.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QuizType quizType = QuizType.PRACTICE;

    private int totalQuestions;
    @Builder.Default
    private int correct = 0;
    @Builder.Default
    private int incorrect = 0;
    @Builder.Default
    private int skipped = 0;
    @Builder.Default
    private int marks = 0;

    @CreationTimestamp
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Builder.Default
    private boolean completed = false;

    public enum QuizType {
        PRACTICE, MOCK_TEST, REVISION, RANDOM, PREVIOUS_YEAR, DIFFICULTY_BASED
    }
}
