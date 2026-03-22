package com.neetpg.platform.service;

import com.neetpg.platform.dto.QuestionDto;
import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Question;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.exception.BadRequestException;
import com.neetpg.platform.exception.ResourceNotFoundException;
import com.neetpg.platform.repository.AttemptRepository;
import com.neetpg.platform.repository.BookmarkRepository;
import com.neetpg.platform.repository.ChapterRepository;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.repository.QuizSessionRepository;
import com.neetpg.platform.repository.SpacedRepetitionRepository;
import com.neetpg.platform.repository.SubjectRepository;
import com.neetpg.platform.repository.UserNoteRepository;
import com.neetpg.platform.util.DatabaseSeeder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final AttemptRepository attemptRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserNoteRepository userNoteRepository;
    private final SpacedRepetitionRepository spacedRepetitionRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final DatabaseSeeder databaseSeeder;
    private final EntityManager entityManager;
    private final ReentrantLock rebuildLock = new ReentrantLock();

    @Transactional
    public QuestionDto.AdminQuestionResponse createQuestion(QuestionDto.CreateRequest request) {
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        Question question = Question.builder()
                .chapter(chapter)
                .questionText(request.getQuestionText())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(request.getCorrectAnswer().toUpperCase())
                .explanation(request.getExplanation())
                .difficulty(request.getDifficulty() != null ?
                        Question.Difficulty.valueOf(request.getDifficulty().toUpperCase()) :
                        Question.Difficulty.MEDIUM)
                .source(request.getSource())
                .tags(request.getTags())
                .previousYear(request.isPreviousYear())
                .build();

        question = questionRepository.save(question);
        return mapToAdminResponse(question);
    }

    @Transactional
    public QuestionDto.AdminQuestionResponse updateQuestion(Long id, QuestionDto.CreateRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (request.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
            question.setChapter(chapter);
        }

        question.setQuestionText(request.getQuestionText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectAnswer(request.getCorrectAnswer().toUpperCase());
        question.setExplanation(request.getExplanation());
        if (request.getDifficulty() != null) {
            question.setDifficulty(Question.Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        }
        question.setSource(request.getSource());
        question.setTags(request.getTags());
        question.setPreviousYear(request.isPreviousYear());

        question = questionRepository.save(question);
        return mapToAdminResponse(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found");
        }
        questionRepository.deleteById(id);
    }

    @Transactional
    public int uploadCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return 0;

            // Skip header row
            int count = 0;
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 8) continue;

                try {
                    String subjectName = row[0].trim();
                    String chapterName = row[1].trim();

                    Subject subject = subjectRepository.findByName(subjectName)
                            .orElseGet(() -> subjectRepository.save(
                                    Subject.builder().name(subjectName).build()));

                    List<Chapter> chapters = chapterRepository.findBySubjectId(subject.getId());
                    Chapter chapter = chapters.stream()
                            .filter(c -> c.getName().equals(chapterName))
                            .findFirst()
                            .orElseGet(() -> chapterRepository.save(
                                    Chapter.builder().name(chapterName).subject(subject).build()));

                    Question question = Question.builder()
                            .chapter(chapter)
                            .questionText(row[2].trim())
                            .optionA(row[3].trim())
                            .optionB(row[4].trim())
                            .optionC(row[5].trim())
                            .optionD(row[6].trim())
                            .correctAnswer(row[7].trim().toUpperCase())
                            .explanation(row.length > 8 ? row[8].trim() : "")
                            .difficulty(row.length > 9 ? Question.Difficulty.valueOf(row[9].trim().toUpperCase()) : Question.Difficulty.MEDIUM)
                            .source(row.length > 10 ? row[10].trim() : "")
                            .tags(row.length > 11 ? row[11].trim() : "")
                            .previousYear(row.length > 12 && Boolean.parseBoolean(row[12].trim()))
                            .build();

                    questionRepository.save(question);
                    count++;
                } catch (Exception e) {
                    // Skip invalid rows
                }
            }
            return count;
        } catch (IOException | CsvException e) {
            throw new BadRequestException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public Subject createSubject(String name) {
        if (subjectRepository.existsByName(name)) {
            throw new BadRequestException("Subject already exists");
        }
        return subjectRepository.save(Subject.builder().name(name).build());
    }

    public Chapter createChapter(String name, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        return chapterRepository.save(Chapter.builder().name(name).subject(subject).build());
    }

    @Transactional
    public Map<String, Object> rebuildQuestionBank(boolean dryRun, boolean resourceOnly, List<String> keepQuestionSubjects) {
        if (!rebuildLock.tryLock()) {
            Map<String, Object> busy = new HashMap<>();
            busy.put("status", "busy");
            busy.put("message", "A rebuild is already in progress. Please retry shortly.");
            busy.put("dryRun", dryRun);
            busy.put("resourceOnly", resourceOnly);
            return busy;
        }

        try {
        long beforeSubjects = subjectRepository.count();
        long beforeChapters = chapterRepository.count();
        long beforeQuestions = questionRepository.count();
        List<String> keepSubjects = Optional.ofNullable(keepQuestionSubjects)
                .orElse(List.of())
                .stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("dryRun", dryRun);
        result.put("resourceOnly", resourceOnly);
        result.put("keepQuestionSubjects", keepSubjects);
        result.put("beforeSubjects", beforeSubjects);
        result.put("beforeChapters", beforeChapters);
        result.put("beforeQuestions", beforeQuestions);

        if (dryRun) {
            result.put("deletedSubjects", 0);
            result.put("deletedChapters", 0);
            result.put("deletedQuestions", 0);
            result.put("afterSubjects", beforeSubjects);
            result.put("afterChapters", beforeChapters);
            result.put("afterQuestions", beforeQuestions);
            return result;
        }

        // Fast, FK-safe reset across the full question bank graph.
        entityManager.createNativeQuery(
            "TRUNCATE TABLE attempts, bookmarks, user_notes, spaced_repetition, quiz_sessions, questions, chapters, subjects RESTART IDENTITY CASCADE"
        ).executeUpdate();
        entityManager.clear();

        if (resourceOnly) {
            databaseSeeder.backfillFromResourcesWithoutFallback();
        } else {
            databaseSeeder.backfillFromResources();
        }

        long deletedByFilter = 0;
        if (!keepSubjects.isEmpty()) {
            for (Subject subject : subjectRepository.findAll()) {
                boolean keep = keepSubjects.stream().anyMatch(s -> s.equalsIgnoreCase(subject.getName()));
                if (keep) {
                    continue;
                }

                for (Chapter chapter : chapterRepository.findBySubjectId(subject.getId())) {
                    List<Question> chapterQuestions = questionRepository.findByChapterId(chapter.getId());
                    if (!chapterQuestions.isEmpty()) {
                        deletedByFilter += chapterQuestions.size();
                        questionRepository.deleteAllInBatch(chapterQuestions);
                    }
                }
            }
        }

        result.put("deletedSubjects", beforeSubjects);
        result.put("deletedChapters", beforeChapters);
        result.put("deletedQuestions", beforeQuestions);
        result.put("deletedQuestionsByFilter", deletedByFilter);
        result.put("afterSubjects", subjectRepository.count());
        result.put("afterChapters", chapterRepository.count());
        result.put("afterQuestions", questionRepository.count());
        return result;
        } finally {
            rebuildLock.unlock();
        }
    }

    @Transactional
    public Map<String, Object> replaceChapterQuestionsFromResource(String subjectName, String chapterName, boolean dryRun) {
        if (subjectName == null || subjectName.isBlank() || chapterName == null || chapterName.isBlank()) {
            throw new BadRequestException("subjectName and chapterName are required");
        }

        Subject subject = subjectRepository.findAll().stream()
                .filter(s -> s.getName() != null)
                .filter(s -> s.getName().equalsIgnoreCase(subjectName.trim()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectName));

        Chapter chapter = chapterRepository.findBySubjectId(subject.getId()).stream()
                .filter(c -> c.getName() != null)
                .filter(c -> c.getName().equalsIgnoreCase(chapterName.trim()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found under subject " + subject.getName() + ": " + chapterName));

        String resourceKey = getResourceKeyForSubject(subject.getName());
        SubjectResourceData resourceData = loadSubjectResource(resourceKey, subject.getName());

        ChapterResourceData chapterData = Optional.ofNullable(resourceData.getChapters()).orElse(List.of())
                .stream()
                .filter(ch -> ch.getName() != null)
                .filter(ch -> ch.getName().equalsIgnoreCase(chapter.getName()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found in resources for subject key " + resourceKey + ": " + chapter.getName()));

        List<QuestionResourceData> sourceQuestions = Optional.ofNullable(chapterData.getQuestions()).orElse(List.of());
        if (sourceQuestions.isEmpty()) {
            throw new BadRequestException("No questions found in resource for chapter: " + chapter.getName());
        }

        long existingCount = questionRepository.countByChapterId(chapter.getId());
        int incomingCount = sourceQuestions.size();

        Map<String, Object> result = new HashMap<>();
        result.put("subject", subject.getName());
        result.put("chapter", chapter.getName());
        result.put("resourceKey", resourceKey);
        result.put("existingCount", existingCount);
        result.put("incomingCount", incomingCount);
        result.put("dryRun", dryRun);

        if (dryRun) {
            result.put("deleted", 0);
            result.put("inserted", 0);
            result.put("finalCount", existingCount);
            return result;
        }

        List<Question> replacement = new ArrayList<>(incomingCount);
        for (QuestionResourceData qd : sourceQuestions) {
            Question.Difficulty difficulty;
            try {
                difficulty = qd.getDifficulty() == null
                        ? Question.Difficulty.MEDIUM
                        : Question.Difficulty.valueOf(qd.getDifficulty().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                difficulty = Question.Difficulty.MEDIUM;
            }

            String correct = qd.getCorrectAnswer() == null ? "A" : qd.getCorrectAnswer().trim().toUpperCase(Locale.ROOT);
            if (correct.length() > 1) {
                correct = correct.substring(0, 1);
            }

            replacement.add(Question.builder()
                    .chapter(chapter)
                    .questionText(defaultString(qd.getQuestionText()))
                    .optionA(defaultString(qd.getOptionA()))
                    .optionB(defaultString(qd.getOptionB()))
                    .optionC(defaultString(qd.getOptionC()))
                    .optionD(defaultString(qd.getOptionD()))
                    .correctAnswer(correct)
                    .explanation(defaultString(qd.getExplanation()))
                    .difficulty(difficulty)
                    .source(defaultString(qd.getSource()))
                    .tags(defaultString(qd.getTags()))
                    .previousYear(qd.isPreviousYear())
                    .build());
        }

        List<Question> existing = questionRepository.findByChapterId(chapter.getId());
        if (!existing.isEmpty()) {
            deleteDependentRowsForQuestions(existing);
            questionRepository.deleteAllInBatch(existing);
        }
        questionRepository.saveAll(replacement);

        result.put("deleted", existingCount);
        result.put("inserted", replacement.size());
        result.put("finalCount", questionRepository.countByChapterId(chapter.getId()));
        return result;
    }

    private void deleteDependentRowsForQuestions(List<Question> questions) {
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (questionIds.isEmpty()) {
            return;
        }

        attemptRepository.deleteAllByQuestionIds(questionIds);
        bookmarkRepository.deleteAllByQuestionIds(questionIds);
        userNoteRepository.deleteAllByQuestionIds(questionIds);
        spacedRepetitionRepository.deleteAllByQuestionIds(questionIds);
    }

    private SubjectResourceData loadSubjectResource(String resourceKey, String subjectName) {
        ObjectMapper mapper = new ObjectMapper();

        String indexPath = "/questions/subjects/" + resourceKey + "/index.json";
        try (InputStream indexStream = getClass().getResourceAsStream(indexPath)) {
            if (indexStream != null) {
                SubjectIndexData indexData = mapper.readValue(indexStream, SubjectIndexData.class);
                SubjectResourceData subjectData = new SubjectResourceData();
                subjectData.setSubject(indexData.getSubject() == null || indexData.getSubject().isBlank() ? subjectName : indexData.getSubject());

                List<ChapterResourceData> chapters = new ArrayList<>();
                for (ChapterIndexData chapterIndex : Optional.ofNullable(indexData.getChapters()).orElse(List.of())) {
                    if (chapterIndex == null || chapterIndex.getFile() == null || chapterIndex.getFile().isBlank()) {
                        continue;
                    }

                    String chapterPath = "/questions/subjects/" + resourceKey + "/chapters/" + chapterIndex.getFile();
                    try (InputStream chapterStream = getClass().getResourceAsStream(chapterPath)) {
                        if (chapterStream == null) {
                            continue;
                        }

                        ChapterResourceData chapterData = mapper.readValue(chapterStream, ChapterResourceData.class);
                        if ((chapterData.getName() == null || chapterData.getName().isBlank()) && chapterIndex.getName() != null) {
                            chapterData.setName(chapterIndex.getName());
                        }
                        chapters.add(chapterData);
                    }
                }

                subjectData.setChapters(chapters);
                return subjectData;
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse structured resource index for " + resourceKey + ": " + e.getMessage());
        }

        String legacyFile = getLegacyResourceFileForSubject(subjectName);
        String[] fallbackPaths = new String[] {"/questions/legacy/" + legacyFile, "/questions/" + legacyFile};
        for (String fallbackPath : fallbackPaths) {
            try (InputStream legacyStream = getClass().getResourceAsStream(fallbackPath)) {
                if (legacyStream == null) {
                    continue;
                }
                return mapper.readValue(legacyStream, SubjectResourceData.class);
            } catch (IOException e) {
                throw new BadRequestException("Failed to parse legacy resource file " + fallbackPath + ": " + e.getMessage());
            }
        }

        throw new ResourceNotFoundException("Resource file not found for subject: " + subjectName);
    }

    private String getLegacyResourceFileForSubject(String subjectName) {
        return getSubjectResourceMap().getOrDefault(subjectName.toLowerCase(Locale.ROOT), subjectName.toLowerCase(Locale.ROOT) + ".json");
    }

    private String getResourceKeyForSubject(String subjectName) {
        String resourceFile = getLegacyResourceFileForSubject(subjectName);
        if (resourceFile.endsWith(".json")) {
            return resourceFile.substring(0, resourceFile.length() - 5);
        }
        return resourceFile;
    }

    private Map<String, String> getSubjectResourceMap() {
        Map<String, String> resourceFiles = new LinkedHashMap<>();
        resourceFiles.put("anatomy", "anatomy.json");
        resourceFiles.put("physiology", "physiology.json");
        resourceFiles.put("biochemistry", "biochemistry.json");
        resourceFiles.put("pathology", "pathology.json");
        resourceFiles.put("pharmacology", "pharmacology.json");
        resourceFiles.put("microbiology", "microbiology.json");
        resourceFiles.put("forensic medicine", "forensics.json");
        resourceFiles.put("community medicine", "psm.json");
        resourceFiles.put("ent", "ent.json");
        resourceFiles.put("ophthalmology", "ophthalmology.json");
        resourceFiles.put("medicine", "medicine.json");
        resourceFiles.put("surgery", "surgery.json");
        resourceFiles.put("obstetrics and gynecology", "obgyn.json");
        resourceFiles.put("pediatrics", "pediatrics.json");
        resourceFiles.put("orthopedics", "orthopedics.json");
        resourceFiles.put("dermatology", "dermatology.json");
        resourceFiles.put("psychiatry", "psychiatry.json");
        resourceFiles.put("radiology", "radiology.json");
        resourceFiles.put("anesthesia", "anesthesia.json");
        return resourceFiles;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    @Data
    public static class SubjectResourceData {
        private String subject;
        private List<ChapterResourceData> chapters;
    }

    @Data
    public static class SubjectIndexData {
        private String subject;
        private List<ChapterIndexData> chapters;
    }

    @Data
    public static class ChapterIndexData {
        private String name;
        private String file;
    }

    @Data
    public static class ChapterResourceData {
        private String name;
        private List<QuestionResourceData> questions;
    }

    @Data
    public static class QuestionResourceData {
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctAnswer;
        private String explanation;
        private String difficulty;
        private String source;
        private boolean previousYear;
        private String tags;
    }

    private QuestionDto.AdminQuestionResponse mapToAdminResponse(Question q) {
        return QuestionDto.AdminQuestionResponse.builder()
                .id(q.getId())
                .chapterId(q.getChapter().getId())
                .chapterName(q.getChapter().getName())
                .subjectName(q.getChapter().getSubject().getName())
                .questionText(q.getQuestionText())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .correctAnswer(q.getCorrectAnswer())
                .explanation(q.getExplanation())
                .difficulty(q.getDifficulty().name())
                .source(q.getSource())
                .tags(q.getTags())
                .previousYear(q.isPreviousYear())
                .build();
    }
}
