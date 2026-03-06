package com.neetpg.platform.controller;

import com.neetpg.platform.dto.QuestionDto;
import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.service.AdminService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/questions")
    public ResponseEntity<QuestionDto.AdminQuestionResponse> createQuestion(
            @Valid @RequestBody QuestionDto.CreateRequest request) {
        return ResponseEntity.ok(adminService.createQuestion(request));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionDto.AdminQuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionDto.CreateRequest request) {
        return ResponseEntity.ok(adminService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {
        int count = adminService.uploadCsv(file);
        return ResponseEntity.ok(Map.of("imported", count));
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.createSubject(body.get("name")));
    }

    @PostMapping("/chapters")
    public ResponseEntity<Chapter> createChapter(@RequestBody ChapterRequest request) {
        return ResponseEntity.ok(adminService.createChapter(request.getName(), request.getSubjectId()));
    }

    @Data
    public static class ChapterRequest {
        private String name;
        private Long subjectId;
    }
}
