package com.neetpg.platform.repository;

import com.neetpg.platform.entity.SpacedRepetition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpacedRepetitionRepository extends JpaRepository<SpacedRepetition, Long> {

       @Modifying
       @Query("DELETE FROM SpacedRepetition sr WHERE sr.question.id IN :questionIds")
       int deleteAllByQuestionIds(@Param("questionIds") List<Long> questionIds);

    Optional<SpacedRepetition> findByUserIdAndQuestionId(Long userId, Long questionId);

    @Query("SELECT sr FROM SpacedRepetition sr WHERE sr.user.id = :userId " +
           "AND sr.nextReviewDate <= :now ORDER BY sr.nextReviewDate ASC")
    List<SpacedRepetition> findDueForReview(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT sr.question.id FROM SpacedRepetition sr WHERE sr.user.id = :userId " +
           "AND sr.nextReviewDate <= :now ORDER BY sr.nextReviewDate ASC")
    List<Long> findDueQuestionIds(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(sr) FROM SpacedRepetition sr WHERE sr.user.id = :userId " +
           "AND sr.nextReviewDate <= :now")
    int countDueForReview(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
