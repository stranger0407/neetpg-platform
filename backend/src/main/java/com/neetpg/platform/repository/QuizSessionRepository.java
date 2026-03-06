package com.neetpg.platform.repository;

import com.neetpg.platform.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    List<QuizSession> findByUserIdOrderByStartedAtDesc(Long userId);

    @Query("SELECT qs FROM QuizSession qs LEFT JOIN FETCH qs.chapter c LEFT JOIN FETCH c.subject " +
           "WHERE qs.user.id = :userId ORDER BY qs.startedAt DESC")
    List<QuizSession> findByUserIdWithChapterOrderByStartedAtDesc(@Param("userId") Long userId);

    List<QuizSession> findByUserIdAndQuizType(Long userId, QuizSession.QuizType quizType);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.user.id = :userId AND qs.completed = true " +
           "ORDER BY qs.completedAt DESC")
    List<QuizSession> findCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT qs.user.id, SUM(qs.marks), SUM(qs.correct) " +
           "FROM QuizSession qs WHERE qs.completed = true AND qs.completedAt >= :since " +
           "GROUP BY qs.user.id ORDER BY SUM(qs.marks) DESC")
    List<Object[]> getLeaderboard(@Param("since") LocalDateTime since);

    @Query("SELECT qs.user.id, SUM(qs.marks) " +
           "FROM QuizSession qs WHERE qs.completed = true AND qs.chapter.subject.id = :subjectId " +
           "GROUP BY qs.user.id ORDER BY SUM(qs.marks) DESC")
    List<Object[]> getSubjectLeaderboard(@Param("subjectId") Long subjectId);
}
