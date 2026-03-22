package com.neetpg.platform.repository;

import com.neetpg.platform.entity.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    @Modifying
    @Query("DELETE FROM UserNote n WHERE n.question.id IN :questionIds")
    int deleteAllByQuestionIds(@Param("questionIds") List<Long> questionIds);

    List<UserNote> findByUserId(Long userId);
    Optional<UserNote> findByUserIdAndQuestionId(Long userId, Long questionId);
}
