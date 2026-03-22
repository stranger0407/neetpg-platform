package com.neetpg.platform.repository;

import com.neetpg.platform.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.question.id IN :questionIds")
    int deleteAllByQuestionIds(@Param("questionIds") List<Long> questionIds);

    List<Bookmark> findByUserId(Long userId);
    Optional<Bookmark> findByUserIdAndQuestionId(Long userId, Long questionId);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    void deleteByUserIdAndQuestionId(Long userId, Long questionId);

    @Query("SELECT b.question.id FROM Bookmark b WHERE b.user.id = :userId")
    List<Long> findQuestionIdsByUserId(@Param("userId") Long userId);
}
