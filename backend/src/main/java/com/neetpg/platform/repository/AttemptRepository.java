package com.neetpg.platform.repository;

import com.neetpg.platform.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

       @Modifying
       @Query("DELETE FROM Attempt a WHERE a.question.id IN :questionIds")
       int deleteAllByQuestionIds(@Param("questionIds") List<Long> questionIds);

    List<Attempt> findByUserId(Long userId);

    List<Attempt> findByUserIdAndQuestionId(Long userId, Long questionId);

    List<Attempt> findByQuizSessionId(Long quizSessionId);

    @Query("SELECT a FROM Attempt a JOIN FETCH a.question WHERE a.quizSession.id = :quizSessionId")
    List<Attempt> findByQuizSessionIdWithQuestion(@Param("quizSessionId") Long quizSessionId);

    List<Attempt> findByQuizSessionIdAndIsCorrect(Long quizSessionId, boolean isCorrect);

    long countByUserId(Long userId);

    long countByUserIdAndIsCorrect(Long userId, boolean isCorrect);

    @Query("SELECT a.question.id FROM Attempt a WHERE a.user.id = :userId AND a.isCorrect = false " +
           "GROUP BY a.question.id ORDER BY COUNT(a) DESC")
    List<Long> findMostIncorrectQuestionIds(@Param("userId") Long userId);

    @Query("SELECT a.question.chapter.id, " +
           "COUNT(a), " +
           "SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END) " +
           "FROM Attempt a WHERE a.user.id = :userId GROUP BY a.question.chapter.id")
    List<Object[]> getChapterWiseStats(@Param("userId") Long userId);

    @Query("SELECT a.question.chapter.subject.id, " +
           "COUNT(a), " +
           "SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END) " +
           "FROM Attempt a WHERE a.user.id = :userId GROUP BY a.question.chapter.subject.id")
    List<Object[]> getSubjectWiseStats(@Param("userId") Long userId);

    @Query("SELECT CAST(a.timestamp AS DATE), COUNT(a), " +
           "SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END) " +
           "FROM Attempt a WHERE a.user.id = :userId AND a.timestamp >= :since " +
           "GROUP BY CAST(a.timestamp AS DATE) ORDER BY CAST(a.timestamp AS DATE)")
    List<Object[]> getDailyStats(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(a.timeTaken) FROM Attempt a WHERE a.user.id = :userId AND a.timeTaken IS NOT NULL")
    Double getAverageTimeTaken(@Param("userId") Long userId);
}
