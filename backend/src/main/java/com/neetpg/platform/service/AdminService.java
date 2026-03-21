package com.neetpg.platform.service;

import com.neetpg.platform.dto.QuestionDto;
import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Question;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.exception.BadRequestException;
import com.neetpg.platform.exception.ResourceNotFoundException;
import com.neetpg.platform.repository.ChapterRepository;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.repository.SubjectRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;

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
