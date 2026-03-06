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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final AttemptRepository attemptRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    @Transactional
    public Map<String, Object> startQuiz(Long userId, QuizDto.StartQuizRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int count = request.getQuestionCount() != null ? request.getQuestionCount() : 20;
        QuizSession.QuizType quizType = QuizSession.QuizType.valueOf(
                request.getQuizType() != null ? request.getQuizType() : "PRACTICE");

        List<Question> questions;
        Chapter chapter = null;

        switch (quizType) {
            case PRACTICE:
                if (request.getChapterId() == null) {
                    throw new BadRequestException("Chapter ID required for practice mode");
                }
                chapter = chapterRepository.findById(request.getChapterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
                questions = questionRepository.findRandomByChapterId(
                        request.getChapterId(), PageRequest.of(0, count));
                break;
            case RANDOM:
                questions = questionRepository.findRandom(PageRequest.of(0, count));
                break;
            case PREVIOUS_YEAR:
                questions = questionRepository.findRandomPreviousYear(PageRequest.of(0, count));
                break;
            case DIFFICULTY_BASED:
                Question.Difficulty difficulty = Question.Difficulty.valueOf(
                        request.getDifficulty() != null ? request.getDifficulty() : "MEDIUM");
                questions = questionRepository.findRandomByDifficulty(difficulty, PageRequest.of(0, count));
                break;
            default:
                questions = questionRepository.findRandom(PageRequest.of(0, count));
        }

        if (questions.isEmpty()) {
            throw new BadRequestException("No questions available for the selected criteria");
        }

        QuizSession session = QuizSession.builder()
                .user(user)
                .chapter(chapter)
                .quizType(quizType)
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
    public QuizDto.QuizResult submitQuiz(Long userId, Long sessionId, QuizDto.SubmitQuizRequest request) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to quiz session");
        }
        if (session.isCompleted()) {
            throw new BadRequestException("Quiz already submitted");
        }

        int correct = 0, incorrect = 0, skipped = 0;
        int totalTime = 0;
        List<QuizDto.QuestionWithAnswer> details = new ArrayList<>();

        Set<Long> answeredIds = new HashSet<>();
        Map<Long, QuizDto.SubmitAnswerRequest> answerMap = new HashMap<>();
        if (request.getAnswers() != null) {
            for (QuizDto.SubmitAnswerRequest answer : request.getAnswers()) {
                answerMap.put(answer.getQuestionId(), answer);
                answeredIds.add(answer.getQuestionId());
            }
        }

        Set<Long> bookmarkedIds = new HashSet<>(bookmarkRepository.findQuestionIdsByUserId(userId));

        // Process each answer
        for (Map.Entry<Long, QuizDto.SubmitAnswerRequest> entry : answerMap.entrySet()) {
            Question question = questionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
            QuizDto.SubmitAnswerRequest answer = entry.getValue();

            boolean isSkipped = answer.getSelectedAnswer() == null || answer.getSelectedAnswer().isBlank();
            boolean isCorrect = !isSkipped && answer.getSelectedAnswer().equalsIgnoreCase(question.getCorrectAnswer());

            if (isSkipped) {
                skipped++;
            } else if (isCorrect) {
                correct++;
            } else {
                incorrect++;
            }

            if (answer.getTimeTaken() != null) {
                totalTime += answer.getTimeTaken();
            }

            Attempt attempt = Attempt.builder()
                    .user(session.getUser())
                    .question(question)
                    .quizSession(session)
                    .selectedAnswer(answer.getSelectedAnswer())
                    .isCorrect(isCorrect)
                    .timeTaken(answer.getTimeTaken())
                    .build();
            attemptRepository.save(attempt);

            // Update spaced repetition
            if (!isSkipped) {
                spacedRepetitionService.updateReviewSchedule(userId, question.getId(), isCorrect);
            }

            details.add(QuizDto.QuestionWithAnswer.builder()
                    .id(question.getId())
                    .questionText(question.getQuestionText())
                    .optionA(question.getOptionA())
                    .optionB(question.getOptionB())
                    .optionC(question.getOptionC())
                    .optionD(question.getOptionD())
                    .correctAnswer(question.getCorrectAnswer())
                    .explanation(question.getExplanation())
                    .difficulty(question.getDifficulty().name())
                    .selectedAnswer(answer.getSelectedAnswer())
                    .isCorrect(isCorrect)
                    .timeTaken(answer.getTimeTaken())
                    .bookmarked(bookmarkedIds.contains(question.getId()))
                    .build());
        }

        int marks = (correct * 4) - (incorrect * 1);
        int totalAnswered = correct + incorrect + skipped;
        double accuracy = totalAnswered > 0 ? (correct * 100.0) / (correct + incorrect) : 0;
        double avgTime = totalAnswered > 0 ? (double) totalTime / totalAnswered : 0;

        session.setCorrect(correct);
        session.setIncorrect(incorrect);
        session.setSkipped(skipped);
        session.setMarks(marks);
        session.setCompleted(true);
        session.setCompletedAt(LocalDateTime.now());
        quizSessionRepository.save(session);

        return QuizDto.QuizResult.builder()
                .sessionId(sessionId)
                .totalQuestions(session.getTotalQuestions())
                .correct(correct)
                .incorrect(incorrect)
                .skipped(skipped)
                .marks(marks)
                .accuracy(Math.round(accuracy * 100.0) / 100.0)
                .averageTimeTaken(Math.round(avgTime * 100.0) / 100.0)
                .questionDetails(details)
                .build();
    }

    public QuizDto.QuizResult getQuizResult(Long userId, Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to quiz session");
        }
        if (!session.isCompleted()) {
            throw new BadRequestException("Quiz not yet submitted");
        }

        List<Attempt> attempts = attemptRepository.findByQuizSessionId(sessionId);
        Set<Long> bookmarkedIds = new HashSet<>(bookmarkRepository.findQuestionIdsByUserId(userId));

        List<QuizDto.QuestionWithAnswer> details = attempts.stream()
                .map(a -> {
                    Question q = a.getQuestion();
                    return QuizDto.QuestionWithAnswer.builder()
                            .id(q.getId())
                            .questionText(q.getQuestionText())
                            .optionA(q.getOptionA())
                            .optionB(q.getOptionB())
                            .optionC(q.getOptionC())
                            .optionD(q.getOptionD())
                            .correctAnswer(q.getCorrectAnswer())
                            .explanation(q.getExplanation())
                            .difficulty(q.getDifficulty().name())
                            .selectedAnswer(a.getSelectedAnswer())
                            .isCorrect(a.isCorrect())
                            .timeTaken(a.getTimeTaken())
                            .bookmarked(bookmarkedIds.contains(q.getId()))
                            .build();
                })
                .collect(Collectors.toList());

        int totalTime = attempts.stream()
                .filter(a -> a.getTimeTaken() != null)
                .mapToInt(Attempt::getTimeTaken)
                .sum();
        double avgTime = !attempts.isEmpty() ? (double) totalTime / attempts.size() : 0;

        return QuizDto.QuizResult.builder()
                .sessionId(sessionId)
                .totalQuestions(session.getTotalQuestions())
                .correct(session.getCorrect())
                .incorrect(session.getIncorrect())
                .skipped(session.getSkipped())
                .marks(session.getMarks())
                .accuracy(session.getCorrect() + session.getIncorrect() > 0 ?
                        Math.round(session.getCorrect() * 10000.0 / (session.getCorrect() + session.getIncorrect())) / 100.0 : 0)
                .averageTimeTaken(Math.round(avgTime * 100.0) / 100.0)
                .questionDetails(details)
                .build();
    }

    public List<QuizDto.QuizSessionResponse> getUserSessions(Long userId) {
        return quizSessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(s -> QuizDto.QuizSessionResponse.builder()
                        .id(s.getId())
                        .quizType(s.getQuizType().name())
                        .chapterName(s.getChapter() != null ? s.getChapter().getName() : "Mixed")
                        .subjectName(s.getChapter() != null ? s.getChapter().getSubject().getName() : "Mixed")
                        .totalQuestions(s.getTotalQuestions())
                        .correct(s.getCorrect())
                        .incorrect(s.getIncorrect())
                        .skipped(s.getSkipped())
                        .marks(s.getMarks())
                        .startedAt(s.getStartedAt() != null ? s.getStartedAt().toString() : null)
                        .completedAt(s.getCompletedAt() != null ? s.getCompletedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
