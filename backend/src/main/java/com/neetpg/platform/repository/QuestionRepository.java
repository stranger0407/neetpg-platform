package com.neetpg.platform.repository;

import com.neetpg.platform.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

       @Query("SELECT q.id FROM Question q JOIN q.chapter c JOIN c.subject s ORDER BY q.id")
       List<Long> findEligibleQuestionIdsForDailyChallenge();

    List<Question> findByChapterId(Long chapterId);

    @Query("SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE q.id = :id")
    Optional<Question> findByIdWithChapterAndSubject(@Param("id") Long id);

    @Query("SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE q.id IN :ids")
    List<Question> findByIdInWithChapterAndSubject(@Param("ids") List<Long> ids);

    long countByChapterId(Long chapterId);

    @Query("SELECT q FROM Question q WHERE q.chapter.id = :chapterId ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomByChapterId(@Param("chapterId") Long chapterId, Pageable pageable);

    @Query("SELECT q FROM Question q ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandom(Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.difficulty = :difficulty ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomByDifficulty(@Param("difficulty") Question.Difficulty difficulty, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.previousYear = true ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomPreviousYear(Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.id IN :ids")
    List<Question> findByIdIn(@Param("ids") List<Long> ids);

    @Query(value = "SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))",
           countQuery = "SELECT COUNT(q) FROM Question q WHERE LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE c.subject.id = :subjectId " +
           "AND (LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%')))",
           countQuery = "SELECT COUNT(q) FROM Question q WHERE q.chapter.subject.id = :subjectId AND (LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<Question> searchByKeywordAndSubject(@Param("keyword") String keyword,
                                              @Param("subjectId") Long subjectId,
                                              Pageable pageable);

    @Query(value = "SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE (LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND q.difficulty = :difficulty",
           countQuery = "SELECT COUNT(q) FROM Question q WHERE (LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND q.difficulty = :difficulty")
    Page<Question> searchByKeywordAndDifficulty(@Param("keyword") String keyword, @Param("difficulty") Question.Difficulty difficulty, Pageable pageable);

    @Query(value = "SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE c.subject.id = :subjectId AND (LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND q.difficulty = :difficulty",
           countQuery = "SELECT COUNT(q) FROM Question q WHERE q.chapter.subject.id = :subjectId AND (LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND q.difficulty = :difficulty")
    Page<Question> searchByKeywordAndSubjectAndDifficulty(@Param("keyword") String keyword, @Param("subjectId") Long subjectId, @Param("difficulty") Question.Difficulty difficulty, Pageable pageable);

    @Query(value = "SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE c.subject.id = :subjectId",
           countQuery = "SELECT COUNT(q) FROM Question q WHERE q.chapter.subject.id = :subjectId")
    Page<Question> findBySubject(@Param("subjectId") Long subjectId, Pageable pageable);

    @Query(value = "SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE q.difficulty = :difficulty",
           countQuery = "SELECT COUNT(q) FROM Question q WHERE q.difficulty = :difficulty")
    Page<Question> findByDifficulty(@Param("difficulty") Question.Difficulty difficulty, Pageable pageable);

    @Query(value = "SELECT q FROM Question q JOIN FETCH q.chapter c JOIN FETCH c.subject WHERE c.subject.id = :subjectId AND q.difficulty = :difficulty",
           countQuery = "SELECT COUNT(q) FROM Question q WHERE q.chapter.subject.id = :subjectId AND q.difficulty = :difficulty")
    Page<Question> findBySubjectAndDifficulty(@Param("subjectId") Long subjectId, @Param("difficulty") Question.Difficulty difficulty, Pageable pageable);
}
