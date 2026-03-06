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
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Question> results;
        int clampedSize = Math.min(size, 100);
        PageRequest pageRequest = PageRequest.of(page, clampedSize);

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasSubject = subjectId != null;
        boolean hasDifficulty = difficulty != null && !difficulty.isBlank();

        if (hasKeyword) {
            if (hasSubject && hasDifficulty) {
                results = questionRepository.searchByKeywordAndSubjectAndDifficulty(
                    keyword, subjectId, Question.Difficulty.valueOf(difficulty.toUpperCase()), pageRequest);
            } else if (hasSubject) {
                results = questionRepository.searchByKeywordAndSubject(keyword, subjectId, pageRequest);
            } else if (hasDifficulty) {
                results = questionRepository.searchByKeywordAndDifficulty(
                    keyword, Question.Difficulty.valueOf(difficulty.toUpperCase()), pageRequest);
            } else {
                results = questionRepository.searchByKeyword(keyword, pageRequest);
            }
        } else {
            if (hasSubject && hasDifficulty) {
                results = questionRepository.findBySubjectAndDifficulty(
                    subjectId, Question.Difficulty.valueOf(difficulty.toUpperCase()), pageRequest);
            } else if (hasSubject) {
                results = questionRepository.findBySubject(subjectId, pageRequest);
            } else if (hasDifficulty) {
                results = questionRepository.findByDifficulty(
                    Question.Difficulty.valueOf(difficulty.toUpperCase()), pageRequest);
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Please provide a keyword or select a filter"));
            }
        }

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
