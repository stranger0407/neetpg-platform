package com.neetpg.platform.repository;

import com.neetpg.platform.entity.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    List<UserNote> findByUserId(Long userId);
    Optional<UserNote> findByUserIdAndQuestionId(Long userId, Long questionId);
}
