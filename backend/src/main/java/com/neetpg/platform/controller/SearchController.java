package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Question;
import com.neetpg.platform.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Question> results;
        PageRequest pageRequest = PageRequest.of(page, size);

        if (subjectId != null) {
            results = questionRepository.searchByKeywordAndSubject(keyword, subjectId, pageRequest);
        } else {
            results = questionRepository.searchByKeyword(keyword, pageRequest);
        }

        List<Map<String, Object>> questions = results.getContent().stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("questionText", q.getQuestionText());
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
