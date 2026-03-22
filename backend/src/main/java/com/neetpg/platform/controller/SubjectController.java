package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Question;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.repository.BookmarkRepository;
import com.neetpg.platform.repository.ChapterRepository;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.repository.SubjectRepository;
import com.neetpg.platform.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final BookmarkRepository bookmarkRepository;

    @Value("${spring.datasource.url:unknown}")
    private String dbUrl;
    
    @Value("${PGDATABASE:unknown}")
    private String pgDatabase;

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

    @GetMapping("/practice/chapter/{chapterId}")
    public ResponseEntity<Map<String, Object>> getPracticeQuestions(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long chapterId) {
        List<Question> questions = questionRepository.findByChapterIdWithChapterAndSubject(chapterId);
        if (questions.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "chapterName", "Unknown",
                "subjectName", "Unknown",
                "totalQuestions", 0,
                "questions", List.of()
            ));
        }

        Chapter chapter = questions.get(0).getChapter();
        Set<Long> bookmarkedIds = new HashSet<>(bookmarkRepository.findQuestionIdsByUserId(user.getId()));

        List<Map<String, Object>> questionList = questions.stream().map(q -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", q.getId());
            map.put("questionText", q.getQuestionText());
            map.put("optionA", q.getOptionA());
            map.put("optionB", q.getOptionB());
            map.put("optionC", q.getOptionC());
            map.put("optionD", q.getOptionD());
            map.put("correctAnswer", q.getCorrectAnswer());
            map.put("explanation", q.getExplanation());
            map.put("difficulty", q.getDifficulty().name());
            map.put("tags", q.getTags());
            map.put("bookmarked", bookmarkedIds.contains(q.getId()));
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("chapterName", chapter.getName());
        result.put("subjectName", chapter.getSubject().getName());
        result.put("totalQuestions", questions.size());
        result.put("questions", questionList);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/admin/seed")
    public ResponseEntity<Map<String, Object>> seedDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            long currentCount = subjectRepository.count();
            if (currentCount > 0) {
                response.put("status", "already_seeded");
                response.put("message", "Database already has " + currentCount + " subjects");
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("message", "Seeding would be done here if implementation was available");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/admin/debug/connection")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Map<String, Object>> debugConnection() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("database_url", dbUrl);
        debug.put("pg_database_env", pgDatabase);
        debug.put("subject_count", subjectRepository.count());
        debug.put("system_pgdatabase", System.getenv("PGDATABASE"));
        debug.put("system_pghost", System.getenv("PGHOST"));
        debug.put("system_pguser", System.getenv("PGUSER"));
        return ResponseEntity.ok(debug);
    }
}
