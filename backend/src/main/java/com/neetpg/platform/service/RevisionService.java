package com.neetpg.platform.service;

import com.neetpg.platform.dto.QuizDto;
import com.neetpg.platform.entity.*;
import com.neetpg.platform.exception.BadRequestException;
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
public class RevisionService {

    private final AttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final QuizSessionRepository quizSessionRepository;

    @Transactional
    public Map<String, Object> generateRevisionQuiz(Long userId, int totalQuestions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int weakCount = (int) (totalQuestions * 0.6);      // 60% from weak topics
        int incorrectCount = (int) (totalQuestions * 0.2);  // 20% from incorrect

        Set<Long> selectedIds = new LinkedHashSet<>();

        // Get weak chapter IDs
        List<Object[]> chapterStats = attemptRepository.getChapterWiseStats(userId);
        List<Long> weakChapterIds = new ArrayList<>();
        for (Object[] row : chapterStats) {
            Long chapterId = (Long) row[0];
            long total = (Long) row[1];
            long correct = (Long) row[2];
            double accuracy = total > 0 ? (correct * 100.0) / total : 0;
            if (accuracy < 40) {
                weakChapterIds.add(chapterId);
            }
        }

        // Add questions from weak topics
        if (!weakChapterIds.isEmpty()) {
            for (Long chapterId : weakChapterIds) {
                if (selectedIds.size() >= weakCount) break;
                List<Question> qs = questionRepository.findRandomByChapterId(
                        chapterId, PageRequest.of(0, weakCount / Math.max(weakChapterIds.size(), 1)));
                qs.forEach(q -> selectedIds.add(q.getId()));
            }
        }

        // Add incorrect questions
        List<Long> incorrectIds = attemptRepository.findMostIncorrectQuestionIds(userId);
        for (Long id : incorrectIds) {
            if (selectedIds.size() >= weakCount + incorrectCount) break;
            selectedIds.add(id);
        }

        // Fill with spaced repetition due questions
        List<Long> dueIds = spacedRepetitionService.getDueQuestionIds(userId);
        for (Long id : dueIds) {
            if (selectedIds.size() >= totalQuestions) break;
            selectedIds.add(id);
        }

        // Fill remaining with random
        if (selectedIds.size() < totalQuestions) {
            List<Question> random = questionRepository.findRandom(
                    PageRequest.of(0, totalQuestions - selectedIds.size() + 10));
            for (Question q : random) {
                if (selectedIds.size() >= totalQuestions) break;
                selectedIds.add(q.getId());
            }
        }

        List<Question> questions = questionRepository.findByIdIn(new ArrayList<>(selectedIds));
        Collections.shuffle(questions);

        QuizSession session = QuizSession.builder()
                .user(user)
                .quizType(QuizSession.QuizType.REVISION)
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
        return result;
    }

    public Map<String, Object> getBookmarkedQuiz(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Long> questionIds = bookmarkRepository.findQuestionIdsByUserId(userId);
        if (questionIds.isEmpty()) {
            throw new BadRequestException("No bookmarked questions found");
        }
        List<Question> questions = questionRepository.findByIdIn(questionIds);
        Collections.shuffle(questions);

        QuizSession session = QuizSession.builder()
                .user(user)
                .quizType(QuizSession.QuizType.REVISION)
                .totalQuestions(questions.size())
                .build();
        session = quizSessionRepository.save(session);

        List<QuizDto.QuestionResponse> questionResponses = questions.stream()
                .map(q -> QuizDto.QuestionResponse.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .difficulty(q.getDifficulty().name())
                        .bookmarked(true)
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("questions", questionResponses);
        return result;
    }

    @Transactional
    public Map<String, Object> getDueReviewQuiz(Long userId, int count) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Long> dueIds = spacedRepetitionService.getDueQuestionIds(userId);
        if (dueIds.isEmpty()) {
            throw new BadRequestException("No questions due for review");
        }
        List<Long> limitedIds = dueIds.stream().limit(count).collect(Collectors.toList());

        List<Question> questions = questionRepository.findByIdIn(limitedIds);
        Collections.shuffle(questions);

        QuizSession session = QuizSession.builder()
                .user(user)
                .quizType(QuizSession.QuizType.REVISION)
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
        return result;
    }

    @Transactional
    public Map<String, Object> reattemptIncorrect(Long userId, Long sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify session belongs to the requesting user
        QuizSession originalSession = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz session not found"));
        if (!originalSession.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to quiz session");
        }

        List<Attempt> incorrectAttempts = attemptRepository.findByQuizSessionIdAndIsCorrect(sessionId, false);
        List<Long> questionIds = incorrectAttempts.stream()
                .map(a -> a.getQuestion().getId())
                .distinct()
                .collect(Collectors.toList());

        if (questionIds.isEmpty()) {
            throw new BadRequestException("No incorrect questions found to reattempt");
        }

        List<Question> questions = questionRepository.findByIdIn(questionIds);
        Collections.shuffle(questions);

        QuizSession session = QuizSession.builder()
                .user(user)
                .quizType(QuizSession.QuizType.REVISION)
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
        return result;
    }
}
