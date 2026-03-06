package com.neetpg.platform.controller;

import com.neetpg.platform.entity.Bookmark;
import com.neetpg.platform.entity.Question;
import com.neetpg.platform.entity.User;
import com.neetpg.platform.entity.UserNote;
import com.neetpg.platform.exception.BadRequestException;
import com.neetpg.platform.exception.ResourceNotFoundException;
import com.neetpg.platform.repository.*;
import com.neetpg.platform.security.UserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final UserNoteRepository userNoteRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @PostMapping("/bookmarks/{questionId}")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long questionId) {
        Long userId = userPrincipal.getId();
        Map<String, Object> result = new HashMap<>();

        if (bookmarkRepository.existsByUserIdAndQuestionId(userId, questionId)) {
            bookmarkRepository.findByUserIdAndQuestionId(userId, questionId)
                    .ifPresent(bookmarkRepository::delete);
            result.put("bookmarked", false);
        } else {
            User user = userRepository.findById(userId).orElseThrow();
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
            Bookmark bookmark = Bookmark.builder().user(user).question(question).build();
            bookmarkRepository.save(bookmark);
            result.put("bookmarked", true);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<Map<String, Object>>> getBookmarks(
            @AuthenticationPrincipal UserPrincipal user) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(user.getId());
        List<Map<String, Object>> result = bookmarks.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            Question q = b.getQuestion();
            map.put("id", b.getId());
            map.put("questionId", q.getId());
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
        return ResponseEntity.ok(result);
    }

    @PostMapping("/notes")
    public ResponseEntity<UserNote> saveNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody NoteRequest request) {
        Long userId = userPrincipal.getId();
        UserNote note = userNoteRepository.findByUserIdAndQuestionId(userId, request.getQuestionId())
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    Question question = questionRepository.findById(request.getQuestionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
                    return UserNote.builder().user(user).question(question).build();
                });
        note.setNoteText(request.getNoteText());
        return ResponseEntity.ok(userNoteRepository.save(note));
    }

    @GetMapping("/notes")
    public ResponseEntity<List<UserNote>> getNotes(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(userNoteRepository.findByUserId(user.getId()));
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long noteId) {
        UserNote note = userNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        if (!note.getUser().getId().equals(userPrincipal.getId())) {
            throw new BadRequestException("Unauthorized");
        }
        userNoteRepository.delete(note);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class NoteRequest {
        private Long questionId;
        private String noteText;
    }
}
