package com.neetpg.platform.util;

import com.neetpg.platform.entity.*;
import com.neetpg.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.io.InputStream;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Map<String, List<String>> SUBJECT_CHAPTERS = new LinkedHashMap<>();

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
            "Vitamins and Minerals"
        ));
        SUBJECT_CHAPTERS.put("Pathology", List.of(
            "Cell Injury and Adaptation", "Inflammation", "Hemodynamic Disorders",
            "Neoplasia", "Immunopathology", "Hematopathology",
            "Cardiovascular Pathology", "Respiratory Pathology", "GI Pathology",
            "Renal Pathology", "Hepatobiliary Pathology", "Endocrine Pathology"
        ));
        SUBJECT_CHAPTERS.put("Pharmacology", List.of(
            "General Pharmacology", "Autonomic Nervous System",
            "Cardiovascular Pharmacology", "CNS Pharmacology",
            "Chemotherapy Antimicrobials", "Autacoids",
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
            "Anatomy of Eye", "Diseases of Conjunctiva", "Diseases of Cornea",
            "Diseases of Lens", "Glaucoma", "Diseases of Retina",
            "Diseases of Uveal Tract", "Strabismus", "Optics and Refraction",
            "Neuro-ophthalmology"
        ));
        SUBJECT_CHAPTERS.put("Medicine", List.of(
            "Cardiology", "Pulmonology", "Gastroenterology",
            "Nephrology", "Neurology", "Endocrinology",
            "Hematology", "Rheumatology", "Infectious Diseases",
            "Dermatology in Medicine", "Critical Care", "Poisoning"
        ));
        SUBJECT_CHAPTERS.put("Surgery", List.of(
            "General Surgery Principles", "Wound Healing", "Surgical Infections",
            "Breast Surgery", "Thyroid Surgery", "GI Surgery",
            "Hepatobiliary Surgery", "Vascular Surgery", "Urology",
            "Trauma and Emergency Surgery"
        ));
        SUBJECT_CHAPTERS.put("Obstetrics and Gynecology", List.of(
            "Normal Pregnancy", "Abnormal Pregnancy", "Labor and Delivery",
            "Puerperium", "High Risk Pregnancy", "Contraception",
            "Abnormal Uterine Bleeding", "Pelvic Infections",
            "Benign Gynecological Tumors", "Malignant Gynecological Tumors"
        ));
        SUBJECT_CHAPTERS.put("Pediatrics", List.of(
            "Neonatology", "Growth and Development", "Nutrition in Pediatrics",
            "Infectious Diseases in Children", "Cardiovascular Disorders",
            "Respiratory Disorders in Children", "GI Disorders in Children",
            "CNS Disorders in Children", "Pediatric Nephrology",
            "Hematological Disorders in Children"
        ));
        SUBJECT_CHAPTERS.put("Orthopedics", List.of(
            "General Orthopedics", "Fractures Upper Limb", "Fractures Lower Limb",
            "Spine Disorders", "Joint Disorders", "Bone Tumors",
            "Metabolic Bone Disease", "Infections of Bone",
            "Congenital Orthopedic Disorders", "Sports Medicine"
        ));
        SUBJECT_CHAPTERS.put("Dermatology", List.of(
            "Basic Dermatology", "Bacterial Skin Infections", "Viral Skin Infections",
            "Fungal Skin Infections", "Parasitic Skin Infections",
            "Papulosquamous Disorders", "Vesicobullous Disorders",
            "Connective Tissue Disorders", "Pigmentary Disorders",
            "Skin Tumors"
        ));
        SUBJECT_CHAPTERS.put("Psychiatry", List.of(
            "Classification of Mental Disorders", "Schizophrenia", "Mood Disorders",
            "Anxiety Disorders", "Substance Use Disorders",
            "Personality Disorders", "Child Psychiatry",
            "Psychopharmacology", "Psychotherapy",
            "Forensic Psychiatry and Ethics"
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
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (subjectRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        log.info("Starting database seeding...");

        // Create admin user
        if (!userRepository.existsByEmail("admin@neetpg.com")) {
            userRepository.save(User.builder()
                    .name("Admin")
                    .email("admin@neetpg.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .build());
            log.info("Admin user created: admin@neetpg.com / admin123");
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

        Random random = new Random(42);
        int totalQuestions = 0;

        ObjectMapper mapper = new ObjectMapper();

        for (Map.Entry<String, List<String>> entry : SUBJECT_CHAPTERS.entrySet()) {
            String subjectName = entry.getKey();
            List<String> chapters = entry.getValue();

            Subject subject = subjectRepository.save(
                    Subject.builder().name(subjectName).build());
            log.info("Created subject: {}", subjectName);

            InputStream is = getClass().getResourceAsStream("/questions/" + subjectName.toLowerCase() + ".json");
            SubjectData subjectData = null;
            if (is != null) {
                try {
                    subjectData = mapper.readValue(is, SubjectData.class);
                    log.info("Loaded real questions for subject: {}", subjectName);
                } catch (Exception e) {
                    log.error("Failed to parse JSON for subject: {}", subjectName, e);
                }
            }

            for (String chapterName : chapters) {
                Chapter chapter = chapterRepository.save(
                        Chapter.builder().name(chapterName).subject(subject).build());

                List<Question> questions = new ArrayList<>();
                boolean usedRealData = false;

                if (subjectData != null && subjectData.getChapters() != null) {
                    Optional<ChapterData> chapterDataOpt = subjectData.getChapters().stream()
                            .filter(ch -> ch.getName().equals(chapterName)).findFirst();
                    
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

                if (!usedRealData) {
                    int questionCount = 300 + random.nextInt(50);
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
                }

                questionRepository.saveAll(questions);
                totalQuestions += questions.size();
                log.info("  Created chapter: {} with {} questions (Real Data: {})", chapterName, questions.size(), usedRealData);
            }
        }

        log.info("Database seeding completed! Total questions: {}", totalQuestions);
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
    public static class ChapterData {
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
