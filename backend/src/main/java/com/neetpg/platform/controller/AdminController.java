package com.neetpg.platform.controller;

import com.neetpg.platform.dto.QuestionDto;
import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.service.AdminService;
import com.neetpg.platform.repository.SubjectRepository;
import com.neetpg.platform.util.DatabaseSeeder;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final SubjectRepository subjectRepository;
    private final DatabaseSeeder databaseSeeder;

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

    @PostMapping("/seed-database")
    public ResponseEntity<String> seedDatabase() {
        try {
            long count = subjectRepository.count();
            if (count > 0) {
                return ResponseEntity.ok("Database already has " + count + " subjects. Skipping.");
            }
            
            log.info("Manual database seeding initiated...");
            databaseSeeder.seedManually();
            
            count = subjectRepository.count();
            return ResponseEntity.ok("✅ Database seeded! Total subjects: " + count);
        } catch (Exception e) {
            log.error("Seeding failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    @GetMapping("/database-status")
    public ResponseEntity<String> databaseStatus() {
        long subjects = subjectRepository.count();
        return ResponseEntity.ok("Database Status:\nSubjects: " + subjects + 
                "\nStatus: " + (subjects > 0 ? "✅ Normal" : "⚠️ Empty - POST /api/admin/seed-database"));
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
