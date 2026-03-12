/**
 * Fix script for NEET PG Question JSON files
 *
 * Issues fixed:
 * 1. ALL questions have correctAnswer: "A" - shuffle options so answers are distributed A/B/C/D
 * 2. Chapter names in some JSON files don't match DatabaseSeeder.java expected names
 */

const fs = require('fs');
const path = require('path');

const dir = __dirname;

// Per-subject chapter name mappings: JSON chapter name => DatabaseSeeder expected name
// Only subject+chapters that need renaming are listed here
const subjectChapterMap = {
  "Medicine": {
    "Cardiovascular System":   "Cardiology",
    "Respiratory System":      "Pulmonology",
    "Gastrointestinal System": "Gastroenterology",
    // "Nephrology" stays same
  },
  "Surgery": {
    "General Surgery": "General Surgery Principles",
    "Trauma":          "Trauma and Emergency Surgery",
    // "Orthopedics" -> keep as is, Surgery subject does have no "Orthopedics" chapter
    // Ortho questions in surgery.json won't match any seeder chapter, that's OK
  },
  "Forensic Medicine": {
    "General Forensic Medicine":  "Medical Jurisprudence",
    "Forensic Pathology":         "Forensic Thanatology",
    "Clinical Forensic Medicine": "Forensic Traumatology",
    "Toxicology":                 "Toxicology General",
    "Sexual Jurisprudence":       "Sexual Offences",
  },
  "Community Medicine": {
    "Concepts of Health and Disease":    "Epidemiology",
    "Epidemiology and Biostatistics":    "Biostatistics",
  },
  "ENT": {
    "Ear":                      "Anatomy of Ear",
    "Nose and Paranasal Sinuses":"Anatomy of Nose",
    "Throat":                   "Anatomy of Pharynx",
  },
  "OBGYN": {
    "Obstetrics": "Normal Pregnancy",
    "Gynecology":  "Abnormal Uterine Bleeding",
  },
};

// Also fix subject names in JSON to match DatabaseSeeder keys
const subjectNameMap = {
  "Forensics":  "Forensic Medicine",
  "PSM":        "Community Medicine",
  "OBGYN":      "Obstetrics and Gynecology",
};

// Deterministic pseudo-random number based on seed
function seededRandom(seed) {
  let s = seed;
  return function() {
    s = (s * 1664525 + 1013904223) & 0xffffffff;
    return (s >>> 0) / 0xffffffff;
  };
}

// Shuffle options A/B/C/D while keeping correctAnswer accurate
function shuffleOptions(question, rng) {
  const options = [
    { key: 'A', value: question.optionA },
    { key: 'B', value: question.optionB },
    { key: 'C', value: question.optionC },
    { key: 'D', value: question.optionD },
  ];

  const correctValue = options.find(o => o.key === question.correctAnswer).value;

  // Fisher-Yates shuffle
  for (let i = options.length - 1; i > 0; i--) {
    const j = Math.floor(rng() * (i + 1));
    [options[i], options[j]] = [options[j], options[i]];
  }

  question.optionA = options[0].value;
  question.optionB = options[1].value;
  question.optionC = options[2].value;
  question.optionD = options[3].value;

  // Find new correct answer
  const newCorrectIdx = options.findIndex(o => o.value === correctValue);
  question.correctAnswer = ['A', 'B', 'C', 'D'][newCorrectIdx];

  return question;
}

let totalQuestions = 0;
let distribution = { A: 0, B: 0, C: 0, D: 0 };

const jsonFiles = fs.readdirSync(dir).filter(f => f.endsWith('.json'));
console.log(`Found ${jsonFiles.length} JSON files to process...\n`);

for (const file of jsonFiles) {
  const filePath = path.join(dir, file);
  let data;
  try {
    data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  } catch (e) {
    console.error(`Failed to parse ${file}: ${e.message}`);
    continue;
  }

  if (!data.chapters) {
    console.log(`Skipping ${file} (no chapters)`);
    continue;
  }

  // Fix subject name if needed
  const origSubject = data.subject;
  if (subjectNameMap[data.subject]) {
    data.subject = subjectNameMap[data.subject];
    console.log(`  Renamed subject: "${origSubject}" -> "${data.subject}" in ${file}`);
  }

  const subjectMap = subjectChapterMap[data.subject] || {};
  let qCount = 0;
  let globalIdx = 0;

  for (const chapter of data.chapters) {
    // Rename chapter if needed
    if (subjectMap[chapter.name]) {
      const oldName = chapter.name;
      chapter.name = subjectMap[chapter.name];
      console.log(`  [${data.subject}] Renamed chapter: "${oldName}" -> "${chapter.name}"`);
    }

    if (!chapter.questions) continue;

    for (const question of chapter.questions) {
      // Deterministic seed so result is reproducible
      const seed = Math.abs(file.charCodeAt(0) * 997 + globalIdx * 1009 + 17) & 0x7fffffff;
      const rng = seededRandom(seed);
      shuffleOptions(question, rng);

      distribution[question.correctAnswer]++;
      qCount++;
      globalIdx++;
      totalQuestions++;
    }
  }

  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf8');
  console.log(`✅ Processed ${file}: ${qCount} questions`);
}

console.log(`\n📊 Results:`);
console.log(`   Total questions processed: ${totalQuestions}`);
console.log(`   Answer distribution:`);
console.log(`     A: ${distribution.A} (${((distribution.A/totalQuestions)*100).toFixed(1)}%)`);
console.log(`     B: ${distribution.B} (${((distribution.B/totalQuestions)*100).toFixed(1)}%)`);
console.log(`     C: ${distribution.C} (${((distribution.C/totalQuestions)*100).toFixed(1)}%)`);
console.log(`     D: ${distribution.D} (${((distribution.D/totalQuestions)*100).toFixed(1)}%)`);
console.log(`\n✅ All JSON files updated successfully!`);
