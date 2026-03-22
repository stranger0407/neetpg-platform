package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Question;
import com.neetpg.platform.repository.BookmarkRepository;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final QuestionRepository questionRepository;
    private final BookmarkRepository bookmarkRepository;

    @GetMapping("/chapter/{chapterId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getChapterQuestions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long chapterId) {

        Long userId = userPrincipal.getId();

        // Get all questions for this chapter
        List<Question> questions = questionRepository.findByChapterId(chapterId);

        if (questions.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("questions", List.of());
            response.put("totalQuestions", 0);
            response.put("chapterName", "");
            response.put("subjectName", "");
            return ResponseEntity.ok(response);
        }

        // Get bookmarked question IDs for this user
        Set<Long> bookmarkedIds = new HashSet<>(bookmarkRepository.findQuestionIdsByUserId(userId));

        // Get chapter and subject names from the first question
        String chapterName = questions.get(0).getChapter().getName();
        String subjectName = questions.get(0).getChapter().getSubject().getName();

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
            map.put("difficulty", q.getDifficulty().name());
            map.put("bookmarked", bookmarkedIds.contains(q.getId()));
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("questions", questionList);
        response.put("totalQuestions", questionList.size());
        response.put("chapterName", chapterName);
        response.put("subjectName", subjectName);

        return ResponseEntity.ok(response);
    }
}
