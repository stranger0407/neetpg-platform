package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Question;
import com.neetpg.platform.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final QuestionRepository questionRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int clampedSize = Math.min(size, 100);
        PageRequest pageRequest = PageRequest.of(page, clampedSize);

        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        String normalizedDifficulty = difficulty != null && !difficulty.isBlank() ? difficulty.trim().toUpperCase() : null;
        Question.Difficulty parsedDifficulty = null;

        if (normalizedDifficulty != null) {
            try {
                parsedDifficulty = Question.Difficulty.valueOf(normalizedDifficulty);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid difficulty value"));
            }
        }

        if (normalizedKeyword == null && subjectId == null && chapterId == null && parsedDifficulty == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please provide a keyword or select a filter"));
        }

        Page<Question> results = questionRepository.searchWithFilters(
                normalizedKeyword,
                subjectId,
                chapterId,
                parsedDifficulty,
                pageRequest
        );

        List<Map<String, Object>> questions = results.getContent().stream().map(q -> {
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
            map.put("chapterName", q.getChapter().getName());
            map.put("subjectName", q.getChapter().getSubject().getName());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("questions", questions);
        response.put("totalElements", results.getTotalElements());
        response.put("totalPages", results.getTotalPages());
        response.put("currentPage", results.getNumber());

        return ResponseEntity.ok(response);
    }
}
