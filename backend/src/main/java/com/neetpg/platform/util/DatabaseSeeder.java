package com.neetpg.platform.util;

import com.neetpg.platform.entity.*;
import com.neetpg.platform.repository.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserNoteRepository userNoteRepository;
    private final SpacedRepetitionRepository spacedRepetitionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.backfill-on-startup:false}")
    private boolean backfillOnStartup;

    @Value("${app.seed.admin.email:admin@neetpg.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.seed.force-admin-password-reset:false}")
    private boolean forceAdminPasswordReset;

    private static final Map<String, List<String>> SUBJECT_CHAPTERS = new LinkedHashMap<>();
    private static final Map<String, String> SUBJECT_RESOURCE_FILES = new HashMap<>();

    static {
        SUBJECT_CHAPTERS.put("Anatomy", List.of(
            "Upper Limb", "Lower Limb", "Thorax", "Abdomen", "Head and Neck",
            "Neuroanatomy", "Embryology General", "Histology", "Osteology",
            "Genetics and Molecular Biology"
        ));
        SUBJECT_CHAPTERS.put("Physiology", List.of(
            "General Physiology", "Nerve Muscle Physiology", "Blood",
            "Cardiovascular System", "Respiratory System", "Renal Physiology",
            "GI Physiology", "Endocrine System", "Neurophysiology",
            "Reproductive Physiology"
        ));
        SUBJECT_CHAPTERS.put("Biochemistry", List.of(
            "Chemistry of Carbohydrates", "Chemistry of Lipids", "Chemistry of Proteins",
            "Enzymes", "Carbohydrate Metabolism", "Lipid Metabolism",
            "Amino Acid Metabolism", "Nucleotide Metabolism", "Molecular Biology",
            "Vitamins and Minerals", "Integration and Clinical Correlations"
        ));
        SUBJECT_CHAPTERS.put("Pathology", List.of(
            "Cell Injury and Adaptation", "Inflammation", "Hemodynamic Disorders",
            "Neoplasia", "Immunopathology", "Hematopathology",
            "Cardiovascular Pathology", "Respiratory Pathology", "GI Pathology",
            "Renal Pathology", "Hepatobiliary Pathology", "Endocrine Pathology"
        ));
        SUBJECT_CHAPTERS.put("Pharmacology", List.of(
            "General Pharmacology", "Autonomic Nervous System",
            "Cardiovascular Pharmacology", "Chemotherapy (Antimicrobials & Oncology)",
            "CNS Pharmacology", "Chemotherapy Antimicrobials", "Autacoids",
            "Endocrine Pharmacology", "GI Pharmacology",
            "Respiratory Pharmacology", "Chemotherapy Anticancer"
        ));
        SUBJECT_CHAPTERS.put("Microbiology", List.of(
            "General Microbiology", "Immunology", "Bacteriology General",
            "Gram Positive Cocci", "Gram Negative Bacilli", "Mycobacteria",
            "Virology General", "DNA Viruses", "RNA Viruses",
            "Mycology and Parasitology"
        ));
        SUBJECT_CHAPTERS.put("Forensic Medicine", List.of(
            "Forensic Thanatology", "Forensic Traumatology", "Toxicology General",
            "Medical Jurisprudence", "Personal Identification",
            "Asphyxia", "Sexual Offences", "Regional Injuries",
            "Thermal Injuries", "Forensic Psychiatry"
        ));
        SUBJECT_CHAPTERS.put("Community Medicine", List.of(
            "Epidemiology", "Biostatistics", "Nutrition",
            "Communicable Diseases", "Non-Communicable Diseases",
            "Maternal and Child Health", "National Health Programs",
            "Environment and Health", "Health Care Delivery",
            "Demography and Family Planning"
        ));
        SUBJECT_CHAPTERS.put("ENT", List.of(
            "Anatomy of Ear", "Diseases of External Ear", "Diseases of Middle Ear",
            "Diseases of Inner Ear", "Anatomy of Nose", "Diseases of Nose and Sinuses",
            "Anatomy of Pharynx", "Diseases of Pharynx", "Anatomy of Larynx",
            "Diseases of Larynx"
        ));
        SUBJECT_CHAPTERS.put("Ophthalmology", List.of(
            "Ophthalmology General", "Anatomy of Eye", "Diseases of Conjunctiva",
            "Diseases of Cornea", "Diseases of Lens", "Glaucoma",
            "Diseases of Retina", "Diseases of Uveal Tract", "Strabismus",
            "Optics and Refraction", "Neuro-ophthalmology"
        ));
        SUBJECT_CHAPTERS.put("Medicine", List.of(
            "Cardiology", "Pulmonology", "Gastroenterology",
            "Nephrology", "Neurology", "Endocrinology",
            "Hematology", "Rheumatology", "Infectious Diseases",
            "Dermatology in Medicine", "Critical Care", "Poisoning"
        ));
        SUBJECT_CHAPTERS.put("Surgery", List.of(
            "General Surgery Principles", "Trauma and Emergency Surgery",
            "Orthopedics", "Wound Healing", "Surgical Infections",
            "Breast Surgery", "Thyroid Surgery", "GI Surgery",
            "Hepatobiliary Surgery", "Vascular Surgery", "Urology"
        ));
        SUBJECT_CHAPTERS.put("Obstetrics and Gynecology", List.of(
            "Obstetrics", "Gynecology", "Normal Pregnancy", "Abnormal Pregnancy",
            "Labor and Delivery", "Puerperium", "High Risk Pregnancy",
            "Contraception", "Abnormal Uterine Bleeding", "Pelvic Infections",
            "Benign Gynecological Tumors", "Malignant Gynecological Tumors"
        ));
        SUBJECT_CHAPTERS.put("Pediatrics", List.of(
            "Pediatrics General", "Neonatology", "Growth and Development",
            "Nutrition in Pediatrics", "Infectious Diseases in Children",
            "Cardiovascular Disorders", "Respiratory Disorders in Children",
            "GI Disorders in Children", "CNS Disorders in Children",
            "Pediatric Nephrology", "Hematological Disorders in Children"
        ));
        SUBJECT_CHAPTERS.put("Orthopedics", List.of(
            "General Orthopedics", "Fractures Upper Limb", "Fractures Lower Limb",
            "Spine Disorders", "Joint Disorders", "Bone Tumors",
            "Metabolic Bone Disease", "Infections of Bone",
            "Congenital Orthopedic Disorders", "Sports Medicine"
        ));
        SUBJECT_CHAPTERS.put("Dermatology", List.of(
            "Dermatology General", "Basic Dermatology", "Bacterial Skin Infections",
            "Viral Skin Infections", "Fungal Skin Infections",
            "Parasitic Skin Infections", "Papulosquamous Disorders",
            "Vesicobullous Disorders", "Connective Tissue Disorders",
            "Pigmentary Disorders", "Skin Tumors"
        ));
        SUBJECT_CHAPTERS.put("Psychiatry", List.of(
            "Classification of Mental Disorders", "Schizophrenia",
            "Mood Disorders", "Anxiety and Stress-Related Disorders",
            "Substance Use Disorders", "Personality Disorders",
            "Child and Adolescent Psychiatry", "Psychopharmacology",
            "Psychotherapy and Behavioral Sciences",
            "Schizophrenia and Psychotic Disorders", "Psychosomatic Disorders",
            "Sleep Disorders", "Psychiatric Ethics and Laws"
        ));
        SUBJECT_CHAPTERS.put("Radiology", List.of(
            "Basic Radiology Physics", "X-Ray Imaging", "CT Scan",
            "MRI Imaging", "Ultrasound", "Nuclear Medicine",
            "Chest Radiology", "Abdominal Radiology",
            "Musculoskeletal Radiology", "Neuroradiology"
        ));
        SUBJECT_CHAPTERS.put("Anesthesia", List.of(
            "General Anesthesia", "Local Anesthesia", "Regional Anesthesia",
            "Preoperative Assessment", "Airway Management",
            "Fluid and Blood Therapy", "Monitoring in Anesthesia",
            "Pain Management", "ICU and Critical Care",
            "Cardiopulmonary Resuscitation"
        ));

        SUBJECT_RESOURCE_FILES.put("Anatomy", "anatomy.json");
        SUBJECT_RESOURCE_FILES.put("Physiology", "physiology.json");
        SUBJECT_RESOURCE_FILES.put("Biochemistry", "biochemistry.json");
        SUBJECT_RESOURCE_FILES.put("Pathology", "pathology.json");
        SUBJECT_RESOURCE_FILES.put("Pharmacology", "pharmacology.json");
        SUBJECT_RESOURCE_FILES.put("Microbiology", "microbiology.json");
        SUBJECT_RESOURCE_FILES.put("Forensic Medicine", "forensics.json");
        SUBJECT_RESOURCE_FILES.put("Community Medicine", "psm.json");
        SUBJECT_RESOURCE_FILES.put("ENT", "ent.json");
        SUBJECT_RESOURCE_FILES.put("Ophthalmology", "ophthalmology.json");
        SUBJECT_RESOURCE_FILES.put("Medicine", "medicine.json");
        SUBJECT_RESOURCE_FILES.put("Surgery", "surgery.json");
        SUBJECT_RESOURCE_FILES.put("Obstetrics and Gynecology", "obgyn.json");
        SUBJECT_RESOURCE_FILES.put("Pediatrics", "pediatrics.json");
        SUBJECT_RESOURCE_FILES.put("Orthopedics", "orthopedics.json");
        SUBJECT_RESOURCE_FILES.put("Dermatology", "dermatology.json");
        SUBJECT_RESOURCE_FILES.put("Psychiatry", "psychiatry.json");
        SUBJECT_RESOURCE_FILES.put("Radiology", "radiology.json");
        SUBJECT_RESOURCE_FILES.put("Anesthesia", "anesthesia.json");
    }

    @Override
    public void run(String... args) {
        ensureDefaultUsers();

        if (!backfillOnStartup) {
            log.info("Startup resource backfill is disabled (app.seed.backfill-on-startup=false)");
            return;
        }

        backfillFromResources();
    }

    public void backfillFromResources() {
        long existingQuestionCount = questionRepository.count();
        if (existingQuestionCount > 0) {
            log.info("Questions already exist ({}). Backfilling only missing chapter question pools...", existingQuestionCount);
        }

        log.info("Starting database seeding for missing question pool...");

        ensureDefaultUsers();

        seedSubjectsChaptersAndQuestions(true);
    }

    public void backfillFromResourcesWithoutFallback() {
        long existingQuestionCount = questionRepository.count();
        if (existingQuestionCount > 0) {
            log.info("Questions already exist ({}). Backfilling only missing chapter question pools from curated resources...", existingQuestionCount);
        }

        log.info("Starting resource-only database seeding for missing question pool...");

        ensureDefaultUsers();

        seedSubjectsChaptersAndQuestions(false);
    }

    /**
     * Force re-seed: replaces old dummy/fallback questions with real JSON data.
     * Detects chapters that have procedurally generated questions (dummy text) and
     * replaces them with curated questions from JSON resource files.
     * Returns a summary map of what was reseeded.
     */
    @Transactional
    public Map<String, Object> reseedFromResources() {
        log.info("Starting force re-seed from JSON resources...");
        ensureDefaultUsers();

        ObjectMapper mapper = new ObjectMapper();
        int totalReplaced = 0;
        int totalNewQuestions = 0;
        List<String> reseededChapters = new ArrayList<>();
        List<String> skippedChapters = new ArrayList<>();
        List<String> newChapters = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : SUBJECT_CHAPTERS.entrySet()) {
            String subjectName = entry.getKey();

            Subject subject = subjectRepository.findByName(subjectName)
                .orElseGet(() -> {
                    Subject created = subjectRepository.save(Subject.builder().name(subjectName).build());
                    log.info("Created subject: {}", subjectName);
                    return created;
                });

            Map<String, Chapter> existingChapters = chapterRepository.findBySubjectId(subject.getId()).stream()
                .collect(Collectors.toMap(ch -> normalizeKey(ch.getName()), ch -> ch, (a, b) -> a));

            String resourceKey = getResourceKeyForSubject(subjectName);
            SubjectData subjectData = loadSubjectData(mapper, subjectName, resourceKey);
            if (subjectData == null || subjectData.getChapters() == null) {
                continue;
            }

            // Collect all chapter names to seed (from SUBJECT_CHAPTERS + index.json)
            LinkedHashSet<String> chapterNamesToSeed = new LinkedHashSet<>(entry.getValue());
            for (ChapterData chapterData : subjectData.getChapters()) {
                if (chapterData != null && chapterData.getName() != null && !chapterData.getName().isBlank()) {
                    chapterNamesToSeed.add(chapterData.getName().trim());
                }
            }

            for (String chapterName : chapterNamesToSeed) {
                String chapterKey = normalizeKey(chapterName);
                Chapter chapter = existingChapters.get(chapterKey);
                if (chapter == null) {
                    chapter = chapterRepository.save(
                            Chapter.builder().name(chapterName).subject(subject).build());
                    existingChapters.put(chapterKey, chapter);
                }

                // Find matching JSON chapter data
                Optional<ChapterData> chapterDataOpt = subjectData.getChapters().stream()
                        .filter(ch -> ch.getName() != null)
                        .filter(ch -> normalizeKey(ch.getName()).equals(chapterKey))
                        .findFirst();

                if (chapterDataOpt.isEmpty() || chapterDataOpt.get().getQuestions() == null
                        || chapterDataOpt.get().getQuestions().isEmpty()) {
                    continue; // No JSON data for this chapter
                }

                long existingCount = questionRepository.countByChapterId(chapter.getId());

                if (existingCount == 0) {
                    // No existing questions — seed fresh
                    List<Question> questions = buildQuestionsFromData(
                            chapter, subjectName, chapterName, chapterDataOpt.get());
                    questionRepository.saveAll(questions);
                    totalNewQuestions += questions.size();
                    newChapters.add(subjectName + " > " + chapterName + " (" + questions.size() + " new)");
                    log.info("  Seeded new chapter: {} > {} with {} questions",
                            subjectName, chapterName, questions.size());
                    continue;
                }

                // Check if existing questions are dummy/fallback data
                List<Question> existingQuestions = questionRepository.findByChapterId(chapter.getId());
                boolean hasDummyData = existingQuestions.stream().anyMatch(q -> isDummyQuestion(q));
                int jsonQuestionCount = chapterDataOpt.get().getQuestions().size();

                if (hasDummyData || existingCount < jsonQuestionCount) {
                    List<Long> questionIds = existingQuestions.stream()
                            .map(Question::getId)
                            .filter(Objects::nonNull)
                            .toList();

                    if (!questionIds.isEmpty()) {
                        // Remove dependent rows first to avoid FK violations when replacing question IDs.
                        attemptRepository.deleteAllByQuestionIds(questionIds);
                        bookmarkRepository.deleteAllByQuestionIds(questionIds);
                        userNoteRepository.deleteAllByQuestionIds(questionIds);
                        spacedRepetitionRepository.deleteAllByQuestionIds(questionIds);
                    }

                    // Replace: delete old, insert new from JSON
                    questionRepository.deleteAllInBatch(existingQuestions);
                    questionRepository.flush();

                    List<Question> questions = buildQuestionsFromData(
                            chapter, subjectName, chapterName, chapterDataOpt.get());
                    questionRepository.saveAll(questions);
                    totalReplaced += questions.size();
                    reseededChapters.add(subjectName + " > " + chapterName
                            + " (" + existingCount + " old -> " + questions.size() + " real)");
                    log.info("  Re-seeded chapter: {} > {} ({} old -> {} real)",
                            subjectName, chapterName, existingCount, questions.size());
                } else {
                    skippedChapters.add(subjectName + " > " + chapterName
                            + " (" + existingCount + " already real)");
                }
            }
        }

        log.info("Force re-seed completed! Replaced: {}, New: {}", totalReplaced, totalNewQuestions);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalReplaced", totalReplaced);
        summary.put("totalNewQuestions", totalNewQuestions);
        summary.put("reseededChapters", reseededChapters);
        summary.put("newChapters", newChapters);
        summary.put("skippedChapters", skippedChapters);
        return summary;
    }

    /**
     * Detect if a question is a dummy/fallback question generated by the old procedural code.
     */
    private boolean isDummyQuestion(Question q) {
        if (q.getQuestionText() == null) return false;
        String text = q.getQuestionText();
        // Procedurally generated questions match patterns like:
        // "... (Q1)", "... (Q23)" at the end
        // or contain "Option A-1", "Option B-2" style options
        if (text.matches(".*\\(Q\\d+\\)\\s*$")) return true;
        // Check for generic placeholder patterns in options
        if (q.getOptionA() != null && q.getOptionA().matches(".*\\(Option [A-D]-\\d+\\)\\s*$")) return true;
        // Check for generic explanation pattern
        if (q.getExplanation() != null && q.getExplanation().contains("Reference: Standard textbook of") 
                && q.getExplanation().matches(".*\\(Q\\d+\\)\\s*$")) return true;
        // Check for starter pool questions
        if (text.startsWith("Starter question ")) return true;
        return false;
    }

    /**
     * Build Question entities from ChapterData JSON.
     */
    private List<Question> buildQuestionsFromData(
            Chapter chapter, String subjectName, String chapterName, ChapterData chapterData) {
        List<Question> questions = new ArrayList<>();
        for (QuestionData qd : chapterData.getQuestions()) {
            questions.add(Question.builder()
                    .chapter(chapter)
                    .questionText(qd.getQuestionText())
                    .optionA(qd.getOptionA())
                    .optionB(qd.getOptionB())
                    .optionC(qd.getOptionC())
                    .optionD(qd.getOptionD())
                    .correctAnswer(qd.getCorrectAnswer())
                    .explanation(qd.getExplanation())
                    .difficulty(Question.Difficulty.valueOf(qd.getDifficulty().toUpperCase()))
                    .source(qd.getSource())
                    .tags(qd.getTags() != null ? qd.getTags() : subjectName + "," + chapterName)
                    .previousYear(qd.isPreviousYear())
                    .build());
        }
        return questions;
    }

    private void ensureDefaultUsers() {
        // Create admin user or reset password when explicitly requested.
        Optional<User> adminUserOpt = userRepository.findByEmail(adminEmail);
        if (adminUserOpt.isEmpty()) {
            userRepository.save(User.builder()
                    .name("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(User.Role.ADMIN)
                    .build());
            log.info("Admin user created: {}", adminEmail);
        } else {
            User adminUser = adminUserOpt.get();
            boolean updated = false;

            if (adminUser.getRole() != User.Role.ADMIN) {
                adminUser.setRole(User.Role.ADMIN);
                updated = true;
            }

            if (forceAdminPasswordReset) {
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                updated = true;
                log.info("Admin password reset was requested by config for: {}", adminEmail);
            }

            if (updated) {
                userRepository.save(adminUser);
            }
        }

        // Create demo student
        if (!userRepository.existsByEmail("student@neetpg.com")) {
            userRepository.save(User.builder()
                    .name("Demo Student")
                    .email("student@neetpg.com")
                    .password(passwordEncoder.encode("student123"))
                    .role(User.Role.STUDENT)
                    .build());
            log.info("Demo student created: student@neetpg.com / student123");
        }
    }

    @Transactional
    protected void seedSubjectsChaptersAndQuestions(boolean allowFallbackQuestions) {

        Random random = new Random(42);
        int totalQuestions = 0;

        ObjectMapper mapper = new ObjectMapper();

        for (Map.Entry<String, List<String>> entry : SUBJECT_CHAPTERS.entrySet()) {
            String subjectName = entry.getKey();
            List<String> chapters = entry.getValue();

            Subject subject = subjectRepository.findByName(subjectName)
                .orElseGet(() -> {
                Subject created = subjectRepository.save(Subject.builder().name(subjectName).build());
                log.info("Created subject: {}", subjectName);
                return created;
                });

            Map<String, Chapter> existingChapters = chapterRepository.findBySubjectId(subject.getId()).stream()
                .collect(Collectors.toMap(ch -> normalizeKey(ch.getName()), ch -> ch, (a, b) -> a));

            String resourceKey = getResourceKeyForSubject(subjectName);
            SubjectData subjectData = loadSubjectData(mapper, subjectName, resourceKey);

            LinkedHashSet<String> chapterNamesToSeed = new LinkedHashSet<>(chapters);
            if (subjectData != null && subjectData.getChapters() != null) {
                for (ChapterData chapterData : subjectData.getChapters()) {
                    if (chapterData != null && chapterData.getName() != null && !chapterData.getName().isBlank()) {
                        chapterNamesToSeed.add(chapterData.getName().trim());
                    }
                }
            }

            for (String chapterName : chapterNamesToSeed) {
                String chapterKey = normalizeKey(chapterName);
                Chapter chapter = existingChapters.get(chapterKey);
                if (chapter == null) {
                    chapter = chapterRepository.save(
                            Chapter.builder().name(chapterName).subject(subject).build());
                    existingChapters.put(chapterKey, chapter);
                }

                if (questionRepository.countByChapterId(chapter.getId()) > 0) {
                    continue;
                }

                List<Question> questions = new ArrayList<>();
                boolean usedRealData = false;

                if (subjectData != null && subjectData.getChapters() != null) {
                    Optional<ChapterData> chapterDataOpt = subjectData.getChapters().stream()
                            .filter(ch -> ch.getName() != null)
                            .filter(ch -> normalizeKey(ch.getName()).equals(chapterKey))
                            .findFirst();
                    
                    if (chapterDataOpt.isPresent() && chapterDataOpt.get().getQuestions() != null && !chapterDataOpt.get().getQuestions().isEmpty()) {
                        for (QuestionData qd : chapterDataOpt.get().getQuestions()) {
                            questions.add(Question.builder()
                                    .chapter(chapter)
                                    .questionText(qd.getQuestionText())
                                    .optionA(qd.getOptionA())
                                    .optionB(qd.getOptionB())
                                    .optionC(qd.getOptionC())
                                    .optionD(qd.getOptionD())
                                    .correctAnswer(qd.getCorrectAnswer())
                                    .explanation(qd.getExplanation())
                                    .difficulty(Question.Difficulty.valueOf(qd.getDifficulty().toUpperCase()))
                                    .source(qd.getSource())
                                    .tags(qd.getTags() != null ? qd.getTags() : subjectName + "," + chapterName)
                                    .previousYear(qd.isPreviousYear())
                                    .build());
                        }
                        usedRealData = true;
                    }
                }

                if (!usedRealData && allowFallbackQuestions) {
                    int questionCount = 30 + random.nextInt(10);
                    for (int i = 1; i <= questionCount; i++) {
                        Question.Difficulty difficulty = switch (random.nextInt(10)) {
                            case 0, 1, 2 -> Question.Difficulty.EASY;
                            case 3, 4, 5, 6 -> Question.Difficulty.MEDIUM;
                            default -> Question.Difficulty.HARD;
                        };

                        String correctAnswer = switch (random.nextInt(4)) {
                            case 0 -> "A";
                            case 1 -> "B";
                            case 2 -> "C";
                            default -> "D";
                        };

                        boolean isPreviousYear = random.nextInt(5) == 0;
                        String source = isPreviousYear ?
                                "NEET PG " + (2015 + random.nextInt(10)) :
                                "Standard Textbook";

                        questions.add(Question.builder()
                                .chapter(chapter)
                                .questionText(generateQuestionText(subjectName, chapterName, i, difficulty))
                                .optionA(generateOption(subjectName, chapterName, "A", i))
                                .optionB(generateOption(subjectName, chapterName, "B", i))
                                .optionC(generateOption(subjectName, chapterName, "C", i))
                                .optionD(generateOption(subjectName, chapterName, "D", i))
                                .correctAnswer(correctAnswer)
                                .explanation(generateExplanation(subjectName, chapterName, correctAnswer, i))
                                .difficulty(difficulty)
                                .source(source)
                                .tags(subjectName + "," + chapterName)
                                .previousYear(isPreviousYear)
                                .build());
                    }
                } else if (!usedRealData) {
                    log.warn("  Skipping fallback question generation for {} > {} (resource-only mode)", subjectName, chapterName);
                }

                questionRepository.saveAll(questions);
                totalQuestions += questions.size();
                log.info("  Created chapter: {} with {} questions (Real Data: {})", chapterName, questions.size(), usedRealData);
            }
        }

        log.info("Database seeding completed! Total questions created in this run: {}", totalQuestions);
    }

    private static String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String getResourceKeyForSubject(String subjectName) {
        String resourceFile = SUBJECT_RESOURCE_FILES.getOrDefault(subjectName, subjectName.toLowerCase(Locale.ROOT) + ".json");
        if (resourceFile.endsWith(".json")) {
            return resourceFile.substring(0, resourceFile.length() - 5);
        }
        return resourceFile;
    }

    private SubjectData loadSubjectData(ObjectMapper mapper, String subjectName, String resourceKey) {
        String indexPath = "/questions/subjects/" + resourceKey + "/index.json";
        try (InputStream indexStream = getClass().getResourceAsStream(indexPath)) {
            if (indexStream != null) {
                SubjectIndexData indexData = mapper.readValue(indexStream, SubjectIndexData.class);
                SubjectData subjectData = new SubjectData();
                subjectData.setSubject(indexData.getSubject() == null || indexData.getSubject().isBlank() ? subjectName : indexData.getSubject());

                List<ChapterData> chapters = new ArrayList<>();
                for (ChapterIndexData chapterIndex : Optional.ofNullable(indexData.getChapters()).orElse(List.of())) {
                    if (chapterIndex == null || chapterIndex.getFile() == null || chapterIndex.getFile().isBlank()) {
                        continue;
                    }

                    String chapterPath = "/questions/subjects/" + resourceKey + "/chapters/" + chapterIndex.getFile();
                    try (InputStream chapterStream = getClass().getResourceAsStream(chapterPath)) {
                        if (chapterStream == null) {
                            log.warn("Missing chapter resource {} for subject {}", chapterPath, subjectName);
                            continue;
                        }

                        ChapterData chapterData = mapper.readValue(chapterStream, ChapterData.class);
                        if ((chapterData.getName() == null || chapterData.getName().isBlank()) && chapterIndex.getName() != null) {
                            chapterData.setName(chapterIndex.getName());
                        }
                        chapters.add(chapterData);
                    } catch (IOException chapterEx) {
                        log.error("Failed to parse chapter JSON {}", chapterPath, chapterEx);
                    }
                }

                subjectData.setChapters(chapters);
                log.info("Loaded structured questions for subject: {} from /questions/subjects/{}/", subjectName, resourceKey);
                return subjectData;
            }
        } catch (IOException indexEx) {
            log.error("Failed to parse index JSON {}", indexPath, indexEx);
        }

        String legacyFile = SUBJECT_RESOURCE_FILES.getOrDefault(subjectName, subjectName.toLowerCase(Locale.ROOT) + ".json");
        String[] fallbackPaths = new String[] {"/questions/legacy/" + legacyFile, "/questions/" + legacyFile};
        for (String fallbackPath : fallbackPaths) {
            try (InputStream is = getClass().getResourceAsStream(fallbackPath)) {
                if (is == null) {
                    continue;
                }
                SubjectData legacyData = mapper.readValue(is, SubjectData.class);
                log.info("Loaded legacy questions for subject: {} from {}", subjectName, fallbackPath);
                return legacyData;
            } catch (IOException legacyEx) {
                log.error("Failed to parse legacy JSON for subject: {} from {}", subjectName, fallbackPath, legacyEx);
            }
        }

        return null;
    }

    private String generateQuestionText(String subject, String chapter, int num, Question.Difficulty diff) {
        String prefix = diff == Question.Difficulty.HARD ? "A 45-year-old patient presents with" :
                diff == Question.Difficulty.MEDIUM ? "Which of the following is true regarding" :
                "What is the correct statement about";

        String[][] templates = {
            {prefix + " symptoms related to %s in the context of %s. What is the most likely diagnosis? (Q%d)"},
            {"In %s, regarding %s, which of the following statements is most accurate? (Q%d)"},
            {"A clinical scenario involving %s specifically in %s domain. The most appropriate next step is? (Q%d)"},
            {"Regarding the mechanism of action in %s related to %s, identify the correct answer. (Q%d)"},
            {"Which of the following best describes the pathophysiology of %s as it relates to %s? (Q%d)"},
            {"A patient with a condition related to %s in %s presents to the emergency department. The investigation of choice is? (Q%d)"},
            {"The gold standard investigation for a condition in %s related to %s is? (Q%d)"},
            {"Which drug is the treatment of choice for a condition in %s related to %s? (Q%d)"},
            {"The characteristic finding in %s associated with %s is? (Q%d)"},
            {"All of the following are features of a condition in %s related to %s EXCEPT? (Q%d)"},
        };

        int idx = num % templates.length;
        return String.format(templates[idx][0], subject, chapter, num);
    }

    private String generateOption(String subject, String chapter, String option, int num) {
        String[][] options = {
            {"Presents with characteristic clinical features of %s - %s (Option %s-%d)"},
            {"Associated with specific findings in %s regarding %s (Option %s-%d)"},
            {"Related to pathological changes in %s within %s (Option %s-%d)"},
            {"Involves therapeutic approach for %s in %s (Option %s-%d)"},
        };
        int idx = (num + option.charAt(0)) % options.length;
        return String.format(options[idx][0], subject, chapter, option, num);
    }

    private String generateExplanation(String subject, String chapter, String correct, int num) {
        return String.format(
            "The correct answer is Option %s. This question tests knowledge of %s in the chapter '%s'. " +
            "Key concepts: The fundamental principles of this topic are essential for NEET PG. " +
            "The correct option describes the most accurate clinical/pathological/pharmacological finding. " +
            "Other options are incorrect because they either describe features of a different condition, " +
            "are partially correct but not the best answer, or contain factual errors. " +
            "Reference: Standard textbook of %s, relevant chapter on %s. (Q%d)",
            correct, subject, chapter, subject, chapter, num
        );
    }

    @Data
    public static class SubjectData {
        private String subject;
        private List<ChapterData> chapters;
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChapterData {
        @JsonAlias({"chapter"})
        private String name;
        private List<QuestionData> questions;
    }

    @Data
    public static class QuestionData {
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
        private String conceptTag;
        private String tags;
    }
}
