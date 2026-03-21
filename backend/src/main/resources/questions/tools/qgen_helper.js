const fs = require('fs');
const path = require('path');

const OUT_DIR = path.join('d:', 'devlopment', 'Neetpg', 'backend', 'src', 'main', 'resources', 'questions');
if (!fs.existsSync(OUT_DIR)) {
  fs.mkdirSync(OUT_DIR, { recursive: true });
}

function q(text, a, b, c, d, ans, exp, diff = "MEDIUM", src = "Standard Textbook", concept = "", prev = false) {
  return {
    questionText: text,
    optionA: a, optionB: b, optionC: c, optionD: d,
    correctAnswer: ans,
    explanation: exp,
    difficulty: diff,
    source: src,
    previousYear: prev,
    conceptTag: concept
  };
}

function writeSubject(subjectName, chaptersData, filename) {
  const data = { subject: subjectName, chapters: [] };
  
  for (const [chName, questions] of chaptersData) {
    const ch = { name: chName, questions: questions };
    for (const quest of ch.questions) {
      quest.tags = `${subjectName},${chName}`;
    }
    data.chapters.push(ch);
  }
  
  const filepath = path.join(OUT_DIR, filename);
  fs.writeFileSync(filepath, JSON.stringify(data, null, 2), 'utf-8');
  
  const total = chaptersData.reduce((acc, curr) => acc + curr[1].length, 0);
  console.log(`Written ${filepath}: ${chaptersData.length} chapters, ${total} questions`);
}

module.exports = { q, writeSubject };
