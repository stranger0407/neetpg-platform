package com.neetpg.platform.service;

import com.neetpg.platform.entity.Chapter;
import com.neetpg.platform.entity.Question;
import com.neetpg.platform.entity.Subject;
import com.neetpg.platform.repository.ChapterRepository;
import com.neetpg.platform.repository.QuestionRepository;
import com.neetpg.platform.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionPoolInitializer {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;

    @Transactional
    public synchronized void ensureMinimumQuestionPool(int minimumQuestions) {
        boolean hasRetrievableQuestions = !questionRepository.findRandom(PageRequest.of(0, 1)).isEmpty();
        if (hasRetrievableQuestions && questionRepository.count() >= minimumQuestions) {
            return;
        }

        Subject subject = subjectRepository.findByName("General")
                .orElseGet(() -> subjectRepository.save(Subject.builder().name("General").build()));

        Chapter chapter = chapterRepository.findBySubjectId(subject.getId()).stream()
                .filter(ch -> "Starter Pool".equals(ch.getName()))
                .findFirst()
                .orElseGet(() -> chapterRepository.save(
                        Chapter.builder().name("Starter Pool").subject(subject).build()));

        int current = (int) questionRepository.count();
        int target = Math.max(minimumQuestions, 50);
        int toCreate = Math.max(0, target - current);
        if (toCreate == 0) {
            return;
        }

        List<Question> generated = new ArrayList<>(toCreate);
        for (int i = 1; i <= toCreate; i++) {
            generated.add(Question.builder()
                    .chapter(chapter)
                    .questionText("Starter question " + i + ": Which option is correct?")
                    .optionA("Option A")
                    .optionB("Option B")
                    .optionC("Option C")
                    .optionD("Option D")
                    .correctAnswer("A")
                    .explanation("Generated starter explanation.")
                    .difficulty(Question.Difficulty.MEDIUM)
                    .source("Starter Seed")
                    .tags("starter,auto-seed")
                    .previousYear(false)
                    .build());
        }

        questionRepository.saveAll(generated);
        log.info("Auto-seeded starter question pool with {} questions", toCreate);
    }
}
