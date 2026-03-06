package com.neetpg.platform.service;

import com.neetpg.platform.entity.Question;
import com.neetpg.platform.entity.SpacedRepetition;
import com.neetpg.platform.entity.User;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.repository.SpacedRepetitionRepository;
import com.neetpg.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private final SpacedRepetitionRepository spacedRepetitionRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateReviewSchedule(Long userId, Long questionId, boolean isCorrect) {
        SpacedRepetition sr = spacedRepetitionRepository
                .findByUserIdAndQuestionId(userId, questionId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    Question question = questionRepository.findById(questionId).orElseThrow();
                    return SpacedRepetition.builder()
                            .user(user)
                            .question(question)
                            .consecutiveCorrect(0)
                            .build();
                });

        sr.setLastReviewedAt(LocalDateTime.now());

        if (isCorrect) {
            sr.setConsecutiveCorrect(sr.getConsecutiveCorrect() + 1);
            int daysUntilNext = switch (sr.getConsecutiveCorrect()) {
                case 1 -> 3;
                case 2 -> 7;
                case 3 -> 14;
                default -> 30;
            };
            sr.setNextReviewDate(LocalDateTime.now().plusDays(daysUntilNext));
        } else {
            sr.setConsecutiveCorrect(0);
            sr.setNextReviewDate(LocalDateTime.now().plusDays(1));
        }

        spacedRepetitionRepository.save(sr);
    }

    public List<Long> getDueQuestionIds(Long userId) {
        return spacedRepetitionRepository.findDueForReview(userId, LocalDateTime.now())
                .stream()
                .map(sr -> sr.getQuestion().getId())
                .collect(Collectors.toList());
    }

    public int getDueCount(Long userId) {
        return spacedRepetitionRepository.findDueForReview(userId, LocalDateTime.now()).size();
    }
}
