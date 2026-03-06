package com.neetpg.platform.repository;

import com.neetpg.platform.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByChapterId(Long chapterId);

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

    @Query("SELECT q FROM Question q WHERE LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(q.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.chapter.subject.id = :subjectId " +
           "AND LOWER(q.questionText) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<Question> searchByKeywordAndSubject(@Param("keyword") String keyword,
                                              @Param("subjectId") Long subjectId,
                                              Pageable pageable);
}
