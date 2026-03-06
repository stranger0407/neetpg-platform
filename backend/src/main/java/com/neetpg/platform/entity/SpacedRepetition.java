package com.neetpg.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "spaced_repetition")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpacedRepetition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Builder.Default
    private int consecutiveCorrect = 0;

    private LocalDateTime nextReviewDate;

    private LocalDateTime lastReviewedAt;
}
