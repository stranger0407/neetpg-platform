package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Question;
import com.neetpg.platform.repository.BookmarkRepository;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.security.UserPrincipal;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final QuestionRepository questionRepository;
    private final BookmarkRepository bookmarkRepository;
    private static final Logger log = LoggerFactory.getLogger(PracticeController.class);

    public PracticeController(QuestionRepository questionRepository, BookmarkRepository bookmarkRepository) {
        this.questionRepository = questionRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @GetMapping("/chapter/{chapterId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getChapterQuestions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long chapterId) {

        Long userId = userPrincipal != null ? userPrincipal.getId() : null;

        // Get all questions for this chapter
        List<Question> questions;
        try {
            questions = questionRepository.findByChapterIdWithChapterAndSubject(chapterId);
        } catch (DataAccessException ex) {
            log.error("Failed to load questions for chapterId={}", chapterId, ex);
            Map<String, Object> response = new HashMap<>();
            response.put("questions", List.of());
            response.put("totalQuestions", 0);
            response.put("chapterName", "");
            response.put("subjectName", "");
            response.put("message", "Temporary database issue. Please retry.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        if (questions.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("questions", List.of());
            response.put("totalQuestions", 0);
            response.put("chapterName", "");
            response.put("subjectName", "");
            return ResponseEntity.ok(response);
        }

        // Get bookmarked question IDs for this user
        Set<Long> bookmarkedIds = Collections.emptySet();
        if (userId != null) {
            try {
                bookmarkedIds = new HashSet<>(bookmarkRepository.findQuestionIdsByUserId(userId));
            } catch (DataAccessException ex) {
                log.warn("Failed to load bookmarked question ids for userId={}. Continuing without bookmark flags.", userId, ex);
            }
        }
        final Set<Long> finalBookmarkedIds = bookmarkedIds;

        // Get chapter and subject names from the first question
        String chapterName = questions.get(0).getChapter() != null && questions.get(0).getChapter().getName() != null
            ? questions.get(0).getChapter().getName()
            : "";
        String subjectName = questions.get(0).getChapter() != null && questions.get(0).getChapter().getSubject() != null
            && questions.get(0).getChapter().getSubject().getName() != null
            ? questions.get(0).getChapter().getSubject().getName()
            : "";

        // We already loaded the chapter's questions above, so avoid a second DB query.
        long totalQuestionsInChapter = questions.size();

        List<Map<String, Object>> questionList = questions.stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("questionText", q.getQuestionText());
            map.put("optionA", q.getOptionA());
            map.put("optionB", q.getOptionB());
            map.put("optionC", q.getOptionC());
            map.put("optionD", q.getOptionD());
            map.put("correctAnswer", q.getCorrectAnswer());
            map.put("explanation", q.getExplanation());
            map.put("difficulty", q.getDifficulty() != null ? q.getDifficulty().name() : "MEDIUM");
            map.put("bookmarked", finalBookmarkedIds.contains(q.getId()));
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("questions", questionList);
        response.put("totalQuestions", totalQuestionsInChapter);
        response.put("chapterName", chapterName);
        response.put("subjectName", subjectName);

        return ResponseEntity.ok(response);
    }
}
