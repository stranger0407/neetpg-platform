package com.neetpg.platform.controller;

import com.neetpg.platform.dto.QuestionDto;
import com.neetpg.platform.entity.Chapter;
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

    @PostMapping("/chapters")
    public ResponseEntity<Chapter> createChapter(@RequestBody ChapterRequest request) {
        return ResponseEntity.ok(adminService.createChapter(request.getName(), request.getSubjectId()));
    }

    @PostMapping("/replace-chapter-from-json")
    public ResponseEntity<Map<String, Object>> replaceChapterFromJson(@RequestBody ReplaceChapterRequest request) {
        boolean dryRun = request.getDryRun() == null || request.getDryRun();
        return ResponseEntity.ok(adminService.replaceChapterQuestionsFromResource(
                request.getSubjectName(),
                request.getChapterName(),
                dryRun
        ));
    }

    @PostMapping("/rebuild-question-bank")
    public ResponseEntity<Map<String, Object>> rebuildQuestionBank(@RequestBody(required = false) RebuildQuestionBankRequest request) {
        boolean dryRun = request == null || request.getDryRun() == null || request.getDryRun();
        boolean resourceOnly = request == null || request.getResourceOnly() == null || request.getResourceOnly();
        return ResponseEntity.ok(adminService.rebuildQuestionBank(
                dryRun,
                resourceOnly,
                request == null ? null : request.getKeepQuestionSubjects()
        ));
    }

    @Data
    public static class ChapterRequest {
        private String name;
        private Long subjectId;
    }

    @Data
    public static class ReplaceChapterRequest {
        private String subjectName;
        private String chapterName;
        private Boolean dryRun;
    }

    @Data
    public static class RebuildQuestionBankRequest {
        private Boolean dryRun;
        private Boolean resourceOnly;
        private java.util.List<String> keepQuestionSubjects;
    }
}
