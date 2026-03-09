package com.neetpg.platform.service;

import com.neetpg.platform.dto.DailyChallengeDto;
import com.neetpg.platform.entity.*;
import com.neetpg.platform.exception.BadRequestException;
import com.neetpg.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyChallengeService {

    private static final int QUESTIONS_PER_CHALLENGE = 10;
    private static final int TIME_LIMIT_MINUTES = 15;

    private final QuestionRepository questionRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final AttemptRepository attemptRepository;
    private final UserRepository userRepository;

    /**
     * Get today's challenge info for the user.
     * If the user has already attempted, returns the result; otherwise returns questions.
     */
    public DailyChallengeDto.ChallengeInfo getTodaysChallenge(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<QuizSession> existing = quizSessionRepository
                .findByUserIdAndQuizTypeAndChallengeDate(userId, QuizSession.QuizType.DAILY_CHALLENGE, today);

        if (existing.isPresent() && existing.get().isCompleted()) {
            // Already attempted and completed – return result
            QuizSession session = existing.get();
            return DailyChallengeDto.ChallengeInfo.builder()
                    .date(today)
                    .questionCount(QUESTIONS_PER_CHALLENGE)
                    .timeLimitMinutes(TIME_LIMIT_MINUTES)
                    .alreadyAttempted(true)
                    .sessionId(session.getId())
                    .result(buildResult(session, today))
                    .build();
        }

        if (existing.isPresent()) {
            // Started but not completed – return questions with the existing session
            QuizSession session = existing.get();
            List<Question> questions = getDailyQuestions(today);
            return DailyChallengeDto.ChallengeInfo.builder()
                    .date(today)
                    .questionCount(QUESTIONS_PER_CHALLENGE)
                    .timeLimitMinutes(TIME_LIMIT_MINUTES)
                    .alreadyAttempted(false)
                    .sessionId(session.getId())
                    .questions(mapQuestions(questions))
                    .build();
        }

        // Not attempted yet – just return challenge info
        List<Question> questions = getDailyQuestions(today);
        return DailyChallengeDto.ChallengeInfo.builder()
                .date(today)
                .questionCount(QUESTIONS_PER_CHALLENGE)
                .timeLimitMinutes(TIME_LIMIT_MINUTES)
                .alreadyAttempted(false)
                .questions(mapQuestions(questions))
                .build();
    }

    /**
     * Start today's challenge – creates a QuizSession.
     */
    @Transactional
    public DailyChallengeDto.ChallengeInfo startChallenge(Long userId) {
        LocalDate today = LocalDate.now();

        // Check if already started
        Optional<QuizSession> existing = quizSessionRepository
                .findByUserIdAndQuizTypeAndChallengeDate(userId, QuizSession.QuizType.DAILY_CHALLENGE, today);

        if (existing.isPresent() && existing.get().isCompleted()) {
            throw new BadRequestException("You have already completed today's challenge");
        }

        if (existing.isPresent()) {
            // Already started, return existing session
            QuizSession session = existing.get();
            List<Question> questions = getDailyQuestions(today);
            return DailyChallengeDto.ChallengeInfo.builder()
                    .date(today)
                    .questionCount(QUESTIONS_PER_CHALLENGE)
                    .timeLimitMinutes(TIME_LIMIT_MINUTES)
                    .alreadyAttempted(false)
                    .sessionId(session.getId())
                    .questions(mapQuestions(questions))
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        QuizSession session = QuizSession.builder()
                .user(user)
                .quizType(QuizSession.QuizType.DAILY_CHALLENGE)
                .totalQuestions(QUESTIONS_PER_CHALLENGE)
                .challengeDate(today)
                .build();
        session = quizSessionRepository.save(session);

        List<Question> questions = getDailyQuestions(today);
        return DailyChallengeDto.ChallengeInfo.builder()
                .date(today)
                .questionCount(QUESTIONS_PER_CHALLENGE)
                .timeLimitMinutes(TIME_LIMIT_MINUTES)
                .alreadyAttempted(false)
                .sessionId(session.getId())
                .questions(mapQuestions(questions))
                .build();
    }

    /**
     * Submit answers for today's challenge.
     */
    @Transactional
    public DailyChallengeDto.ChallengeResult submitChallenge(Long userId, DailyChallengeDto.SubmitRequest request) {
        QuizSession session = quizSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new BadRequestException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }
        if (session.isCompleted()) {
            throw new BadRequestException("Challenge already submitted");
        }
        if (session.getQuizType() != QuizSession.QuizType.DAILY_CHALLENGE) {
            throw new BadRequestException("Not a daily challenge session");
        }

        LocalDate today = LocalDate.now();
        List<Question> questions = getDailyQuestions(today);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // Build a set of answered question IDs
        Set<Long> answeredIds = new HashSet<>();
        if (request.getAnswers() != null) {
            for (DailyChallengeDto.AnswerSubmission ans : request.getAnswers()) {
                answeredIds.add(ans.getQuestionId());
            }
        }

        int correct = 0, incorrect = 0, skipped = 0, marks = 0;

        // Process submitted answers
        if (request.getAnswers() != null) {
            for (DailyChallengeDto.AnswerSubmission ans : request.getAnswers()) {
                Question q = questionMap.get(ans.getQuestionId());
                if (q == null) continue;

                boolean isCorrect = ans.getSelectedAnswer() != null
                        && ans.getSelectedAnswer().equalsIgnoreCase(q.getCorrectAnswer());
                boolean isSkipped = ans.getSelectedAnswer() == null || ans.getSelectedAnswer().isBlank();

                if (isSkipped) {
                    skipped++;
                } else if (isCorrect) {
                    correct++;
                    marks += 4;
                } else {
                    incorrect++;
                    marks -= 1;
                }

                Attempt attempt = Attempt.builder()
                        .user(session.getUser())
                        .question(q)
                        .quizSession(session)
                        .selectedAnswer(ans.getSelectedAnswer())
                        .isCorrect(isCorrect)
                        .timeTaken(ans.getTimeTaken())
                        .build();
                attemptRepository.save(attempt);
            }
        }

        // Count questions that weren't in the submitted answers as skipped
        skipped += (QUESTIONS_PER_CHALLENGE - answeredIds.size());

        session.setCorrect(correct);
        session.setIncorrect(incorrect);
        session.setSkipped(skipped);
        session.setMarks(marks);
        session.setCompleted(true);
        session.setCompletedAt(LocalDateTime.now());
        quizSessionRepository.save(session);

        return buildResult(session, today);
    }

    /**
     * Get the leaderboard for a specific date.
     */
    public List<DailyChallengeDto.LeaderboardEntry> getLeaderboard(LocalDate date) {
        List<Object[]> raw = quizSessionRepository.getDailyChallengeLeaderboard(date);

        List<Long> userIds = raw.stream()
                .map(row -> (Long) row[0])
                .limit(50)
                .collect(Collectors.toList());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<DailyChallengeDto.LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Object[] row : raw) {
            Long uid = (Long) row[0];
            int score = ((Number) row[1]).intValue();
            int corr = ((Number) row[2]).intValue();
            int total = ((Number) row[3]).intValue();
            User user = userMap.get(uid);
            if (user == null) continue;

            entries.add(DailyChallengeDto.LeaderboardEntry.builder()
                    .rank(rank++)
                    .userId(uid)
                    .userName(user.getName())
                    .score(score)
                    .correct(corr)
                    .totalQuestions(total)
                    .build());
            if (rank > 50) break;
        }
        return entries;
    }

    // --- Private helpers ---

    /**
     * Deterministic question selection based on the date.
     * Uses the date as a seed so all users get the same questions for a given day.
     */
    private List<Question> getDailyQuestions(LocalDate date) {
        long totalQuestions = questionRepository.count();
        if (totalQuestions == 0) return Collections.emptyList();

        // Use date to create a deterministic seed
        Random random = new Random(date.hashCode());

        // Get all question IDs and deterministically pick QUESTIONS_PER_CHALLENGE
        List<Long> allIds = questionRepository.findAll().stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        Collections.shuffle(allIds, random);

        int count = Math.min(QUESTIONS_PER_CHALLENGE, allIds.size());
        List<Long> selectedIds = allIds.subList(0, count);

        return questionRepository.findByIdInWithChapterAndSubject(selectedIds);
    }

    private List<DailyChallengeDto.QuestionItem> mapQuestions(List<Question> questions) {
        return questions.stream().map(q -> DailyChallengeDto.QuestionItem.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .subject(q.getChapter().getSubject().getName())
                .chapter(q.getChapter().getName())
                .difficulty(q.getDifficulty().name())
                .build())
                .collect(Collectors.toList());
    }

    private DailyChallengeDto.ChallengeResult buildResult(QuizSession session, LocalDate date) {
        long participants = quizSessionRepository.countDailyChallengeParticipants(date);

        // Calculate rank
        List<Object[]> leaderboard = quizSessionRepository.getDailyChallengeLeaderboard(date);
        int rank = 1;
        for (Object[] row : leaderboard) {
            Long uid = (Long) row[0];
            if (uid.equals(session.getUser().getId())) break;
            rank++;
        }

        double accuracy = session.getTotalQuestions() > 0
                ? (double) session.getCorrect() / session.getTotalQuestions() * 100 : 0;

        return DailyChallengeDto.ChallengeResult.builder()
                .score(session.getMarks())
                .maxScore(session.getTotalQuestions() * 4)
                .correct(session.getCorrect())
                .incorrect(session.getIncorrect())
                .skipped(session.getSkipped())
                .totalQuestions(session.getTotalQuestions())
                .accuracy(Math.round(accuracy * 10.0) / 10.0)
                .rank(rank)
                .totalParticipants((int) participants)
                .build();
    }
}
