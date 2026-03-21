package com.neetpg.platform.service;

import com.neetpg.platform.dto.QuizDto;
import com.neetpg.platform.entity.*;
import com.neetpg.platform.exception.ResourceNotFoundException;
import com.neetpg.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockTestService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final BookmarkRepository bookmarkRepository;
        private final QuestionPoolInitializer questionPoolInitializer;

    @Transactional
    public Map<String, Object> startMockTest(Long userId) {
                questionPoolInitializer.ensureMinimumQuestionPool(200);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Question> questions = questionRepository.findRandom(PageRequest.of(0, 200));

        QuizSession session = QuizSession.builder()
                .user(user)
                .quizType(QuizSession.QuizType.MOCK_TEST)
                .totalQuestions(questions.size())
                .build();
        session = quizSessionRepository.save(session);

        Set<Long> bookmarkedIds = new HashSet<>(bookmarkRepository.findQuestionIdsByUserId(userId));

        List<QuizDto.QuestionResponse> questionResponses = questions.stream()
                .map(q -> QuizDto.QuestionResponse.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .difficulty(q.getDifficulty().name())
                        .tags(q.getTags())
                        .bookmarked(bookmarkedIds.contains(q.getId()))
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("questions", questionResponses);
        result.put("durationMinutes", 210); // 3.5 hours
        return result;
    }
}
