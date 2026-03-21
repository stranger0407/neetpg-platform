package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.repository.ChapterRepository;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;

    @GetMapping("/subjects")
    public ResponseEntity<List<Map<String, Object>>> getSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        List<Map<String, Object>> result = subjects.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("chapterCount", chapterRepository.countBySubjectId(s.getId()));
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/subjects/{subjectId}/chapters")
    public ResponseEntity<List<Map<String, Object>>> getChapters(@PathVariable Long subjectId) {
        List<Chapter> chapters = chapterRepository.findBySubjectId(subjectId);
        List<Map<String, Object>> result = chapters.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("questionCount", questionRepository.countByChapterId(c.getId()));
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
