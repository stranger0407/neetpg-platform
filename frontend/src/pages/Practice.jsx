import { useState, useEffect } from 'react';
import api from '../api';

export default function Practice() {
  // Navigation state
  const [step, setStep] = useState('subjects'); // 'subjects' | 'chapters' | 'questions'
  const [subjects, setSubjects] = useState([]);
  const [chapters, setChapters] = useState([]);
  const [selectedSubject, setSelectedSubject] = useState(null);
  const [selectedChapter, setSelectedChapter] = useState(null);

  // Questions state
  const [questions, setQuestions] = useState([]);
  const [chapterName, setChapterName] = useState('');
  const [subjectName, setSubjectName] = useState('');
  const [totalQuestions, setTotalQuestions] = useState(0);

  // UI state
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedAnswers, setSelectedAnswers] = useState({});
  const [bookmarkedIds, setBookmarkedIds] = useState(new Set());
  const [aiExplanations, setAiExplanations] = useState({});
  const [aiLoading, setAiLoading] = useState({});

  const optionLabels = ['A', 'B', 'C', 'D'];
  const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD'];

  // Load subjects on mount
  useEffect(() => {
    const fetchSubjects = async () => {
      try {
        const res = await api.get('/subjects');
        setSubjects(res.data || []);
      } catch (err) {
        setError('Failed to load subjects.');
      } finally {
        setLoading(false);
      }
    };
    fetchSubjects();
  }, []);

  // Select subject → load chapters
  const handleSelectSubject = async (subject) => {
    setSelectedSubject(subject);
    setLoading(true);
    setError('');
    try {
      const res = await api.get(`/subjects/${subject.id}/chapters`);
      const chapterList = Array.isArray(res.data) ? res.data : res.data?.chapters || [];
      setChapters(chapterList);
      setStep('chapters');
    } catch {
      setError('Failed to load chapters.');
    } finally {
      setLoading(false);
    }
  };

  // Select chapter → load questions
  const handleSelectChapter = async (chapter) => {
    setSelectedChapter(chapter);
    setLoading(true);
    setError('');
    setSelectedAnswers({});
    setAiExplanations({});
    try {
      const res = await api.get(`/practice/chapter/${chapter.id}`);
      const data = res.data || {};
      setQuestions(data.questions || []);
      setChapterName(data.chapterName || chapter.name || '');
      setSubjectName(data.subjectName || selectedSubject?.name || '');
      setTotalQuestions(data.totalQuestions || 0);
      const bIds = new Set();
      (data.questions || []).forEach((q) => {
        if (q.bookmarked) bIds.add(q.id);
      });
      setBookmarkedIds(bIds);
      setStep('questions');
    } catch {
      setError('Failed to load questions.');
    } finally {
      setLoading(false);
    }
  };

  // Navigate back
  const goToSubjects = () => {
    setStep('subjects');
    setSelectedSubject(null);
    setChapters([]);
    setError('');
  };

  const goToChapters = () => {
    setStep('chapters');
    setSelectedChapter(null);
    setQuestions([]);
    setSelectedAnswers({});
    setAiExplanations({});
    setError('');
  };

  // Answer a question
  const selectAnswer = (questionId, label) => {
    if (selectedAnswers[questionId]) return;
    setSelectedAnswers((prev) => ({ ...prev, [questionId]: label }));
  };

  // Toggle bookmark
  const toggleBookmark = async (questionId) => {
    try {
      const res = await api.post(`/bookmarks/${questionId}`);
      setBookmarkedIds((prev) => {
        const next = new Set(prev);
        if (res.data.bookmarked) {
          next.add(questionId);
        } else {
          next.delete(questionId);
        }
        return next;
      });
    } catch {
      // silently fail
    }
  };

  // AI explanation
  const fetchAiExplanation = async (questionId) => {
    if (aiExplanations[questionId] || aiLoading[questionId]) return;
    setAiLoading((prev) => ({ ...prev, [questionId]: true }));
    try {
      const res = await api.get(`/ai/explain/${questionId}`);
      setAiExplanations((prev) => ({ ...prev, [questionId]: res.data.detailedExplanation }));
    } catch {
      setAiExplanations((prev) => ({ ...prev, [questionId]: 'Failed to load AI explanation.' }));
    } finally {
      setAiLoading((prev) => ({ ...prev, [questionId]: false }));
    }
  };

  const answeredCount = Object.keys(selectedAnswers).length;
  const correctCount = questions.filter((q) => selectedAnswers[q.id] === q.correctAnswer).length;

  // ── Loading ──
  if (loading && step === 'subjects' && subjects.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading subjects...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* ── Breadcrumb ── */}
        <div className="flex items-center gap-2 text-sm text-gray-500 mb-6 flex-wrap">
          <button
            onClick={goToSubjects}
            className={`hover:text-indigo-600 transition-colors cursor-pointer ${step === 'subjects' ? 'text-gray-900 font-semibold' : ''}`}
          >
            Practice
          </button>
          {(step === 'chapters' || step === 'questions') && (
            <>
              <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
              <button
                onClick={step === 'questions' ? goToChapters : undefined}
                className={`hover:text-indigo-600 transition-colors cursor-pointer ${step === 'chapters' ? 'text-gray-900 font-semibold' : ''}`}
              >
                {selectedSubject?.name}
              </button>
            </>
          )}
          {step === 'questions' && (
            <>
              <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
              <span className="text-gray-900 font-semibold">{chapterName}</span>
            </>
          )}
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
            {error}
          </div>
        )}

        {/* ── Step 1: Subjects ── */}
        {step === 'subjects' && (
          <>
            <div className="mb-8">
              <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Practice Questions</h1>
              <p className="mt-1 text-gray-500">Choose a subject to start practicing chapter-wise.</p>
            </div>

            {subjects.length === 0 ? (
              <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
                <p className="text-gray-400">No subjects available yet.</p>
              </div>
            ) : (
              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {subjects.map((subject) => (
                  <button
                    key={subject.id}
                    onClick={() => handleSelectSubject(subject)}
                    className="bg-white rounded-xl border border-gray-100 p-6 hover:border-indigo-300 hover:shadow-lg transition-all text-left group cursor-pointer"
                  >
                    <div className="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center mb-4 group-hover:bg-indigo-100 transition-colors">
                      <svg className="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                      </svg>
                    </div>
                    <h3 className="font-semibold text-gray-900 group-hover:text-indigo-600 transition-colors">
                      {subject.name}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1">
                      {subject.chapterCount ?? 0} Chapters
                    </p>
                  </button>
                ))}
              </div>
            )}
          </>
        )}

        {/* ── Step 2: Chapters ── */}
        {step === 'chapters' && (
          <>
            <div className="mb-8">
              <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">{selectedSubject?.name}</h1>
              <p className="mt-1 text-gray-500">Select a chapter to practice all its questions.</p>
            </div>

            {loading ? (
              <div className="flex justify-center py-12">
                <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
              </div>
            ) : chapters.length === 0 ? (
              <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
                <p className="text-gray-400">No chapters available for this subject.</p>
              </div>
            ) : (
              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {chapters.map((chapter) => (
                  <button
                    key={chapter.id}
                    onClick={() => handleSelectChapter(chapter)}
                    className="bg-white rounded-xl border border-gray-100 p-6 hover:border-indigo-300 hover:shadow-lg transition-all text-left group cursor-pointer"
                  >
                    <div className="w-10 h-10 bg-indigo-50 rounded-lg flex items-center justify-center mb-3 group-hover:bg-indigo-100 transition-colors">
                      <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    </div>
                    <h3 className="font-semibold text-gray-900 group-hover:text-indigo-600 transition-colors">
                      {chapter.name}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1">
                      {chapter.questionCount ?? 0} Questions
                    </p>
                  </button>
                ))}
              </div>
            )}
          </>
        )}

        {/* ── Step 3: Questions ── */}
        {step === 'questions' && (
          <>
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
              <div>
                <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">{chapterName}</h1>
                <p className="mt-1 text-gray-500">{subjectName} • {totalQuestions} Questions</p>
              </div>

              {/* Progress tracker */}
              {answeredCount > 0 && (
                <div className="flex items-center gap-3">
                  <div className="text-right">
                    <p className="text-sm font-semibold text-gray-900">{answeredCount} / {totalQuestions} answered</p>
                    <p className="text-xs text-gray-500">{correctCount} correct</p>
                  </div>
                  <div className="w-12 h-12 relative">
                    <svg className="w-12 h-12 -rotate-90" viewBox="0 0 36 36">
                      <path
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                        fill="none" stroke="#e5e7eb" strokeWidth="3"
                      />
                      <path
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                        fill="none" stroke="#6366f1" strokeWidth="3"
                        strokeDasharray={`${(answeredCount / totalQuestions) * 100}, 100`}
                        strokeLinecap="round"
                      />
                    </svg>
                    <span className="absolute inset-0 flex items-center justify-center text-xs font-bold text-indigo-600">
                      {Math.round((answeredCount / totalQuestions) * 100)}%
                    </span>
                  </div>
                </div>
              )}
            </div>

            {loading ? (
              <div className="flex justify-center py-12">
                <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
              </div>
            ) : questions.length === 0 ? (
              <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
                <p className="text-gray-400">No questions available for this chapter.</p>
              </div>
            ) : (
              <div className="space-y-5">
                {questions.map((q, qIndex) => {
                  const userPick = selectedAnswers[q.id];
                  const isAnswered = !!userPick;
                  const isBookmarked = bookmarkedIds.has(q.id);

                  return (
                    <div key={q.id} className="bg-white rounded-xl border border-gray-100 p-5 sm:p-6 transition-all hover:shadow-sm">
                      {/* Question header */}
                      <div className="flex items-start justify-between gap-3 mb-4">
                        <div className="flex items-start gap-3 flex-1">
                          <span className="shrink-0 w-8 h-8 bg-indigo-50 rounded-lg flex items-center justify-center text-sm font-bold text-indigo-600">
                            {qIndex + 1}
                          </span>
                          <p className="text-gray-900 leading-relaxed whitespace-pre-wrap pt-1">
                            {q.questionText}
                          </p>
                        </div>

                        {/* Bookmark button */}
                        <button
                          onClick={() => toggleBookmark(q.id)}
                          className={`shrink-0 p-2 rounded-lg transition-all cursor-pointer ${
                            isBookmarked
                              ? 'text-amber-500 bg-amber-50 hover:bg-amber-100'
                              : 'text-gray-400 hover:text-amber-500 hover:bg-amber-50'
                          }`}
                          title={isBookmarked ? 'Remove bookmark' : 'Bookmark this question'}
                        >
                          <svg className="w-5 h-5" fill={isBookmarked ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                          </svg>
                        </button>
                      </div>

                      {/* Options */}
                      <div className="space-y-2 mb-4">
                        {optionKeys.map((key, idx) => {
                          const label = optionLabels[idx];
                          const isCorrect = label === q.correctAnswer;
                          const isSelected = userPick === label;
                          const isWrongPick = isSelected && !isCorrect;

                          let optionClass = '';
                          let labelClass = '';
                          let iconEl = null;

                          if (isAnswered) {
                            if (isCorrect) {
                              optionClass = 'border-green-300 bg-green-50';
                              labelClass = 'bg-green-600 text-white';
                              iconEl = (
                                <svg className="w-4 h-4 text-green-600 ml-auto shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                </svg>
                              );
                            } else if (isWrongPick) {
                              optionClass = 'border-red-300 bg-red-50';
                              labelClass = 'bg-red-600 text-white';
                              iconEl = (
                                <svg className="w-4 h-4 text-red-600 ml-auto shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                </svg>
                              );
                            } else {
                              optionClass = 'border-gray-100 bg-gray-50 opacity-60';
                              labelClass = 'bg-gray-200 text-gray-500';
                            }
                          } else {
                            optionClass = 'border-gray-200 bg-gray-50 hover:border-indigo-300 hover:bg-indigo-50/50 cursor-pointer';
                            labelClass = 'bg-gray-200 text-gray-600';
                          }

                          return (
                            <button
                              key={key}
                              type="button"
                              disabled={isAnswered}
                              onClick={() => selectAnswer(q.id, label)}
                              className={`w-full flex items-center gap-3 p-3 rounded-lg border-2 transition-all text-left ${optionClass} ${isAnswered ? 'cursor-default' : ''}`}
                            >
                              <span className={`text-xs font-bold w-7 h-7 rounded-md flex items-center justify-center shrink-0 transition-colors ${labelClass}`}>
                                {label}
                              </span>
                              <span className="text-sm text-gray-700 flex-1">{q[key]}</span>
                              {iconEl}
                            </button>
                          );
                        })}
                      </div>

                      {/* Answer feedback */}
                      {isAnswered && (
                        <div className={`mb-3 inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-semibold ${
                          userPick === q.correctAnswer
                            ? 'bg-green-100 text-green-700'
                            : 'bg-red-100 text-red-700'
                        }`}>
                          {userPick === q.correctAnswer ? (
                            <>
                              <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                              </svg>
                              Correct!
                            </>
                          ) : (
                            <>
                              <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                              </svg>
                              Incorrect — Answer is {q.correctAnswer}
                            </>
                          )}
                        </div>
                      )}

                      {/* Explanation */}
                      {isAnswered && q.explanation && (
                        <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                          <p className="text-xs font-semibold text-blue-700 mb-1">Explanation</p>
                          <p className="text-sm text-blue-900 leading-relaxed whitespace-pre-wrap">{q.explanation}</p>
                        </div>
                      )}

                      {/* AI Explanation */}
                      {isAnswered && (
                        <div className="mt-3">
                          {!aiExplanations[q.id] ? (
                            <button
                              onClick={() => fetchAiExplanation(q.id)}
                              disabled={aiLoading[q.id]}
                              className="px-4 py-2 text-sm font-medium text-indigo-600 bg-indigo-50 border border-indigo-200 rounded-lg hover:bg-indigo-100 disabled:opacity-50 cursor-pointer transition-colors"
                            >
                              {aiLoading[q.id] ? (
                                <span className="flex items-center gap-2">
                                  <span className="w-4 h-4 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin" />
                                  Generating AI Explanation...
                                </span>
                              ) : (
                                'Explain with AI'
                              )}
                            </button>
                          ) : (
                            <div className="p-4 bg-purple-50 border border-purple-200 rounded-lg">
                              <p className="text-xs font-semibold text-purple-700 mb-2">AI Detailed Explanation</p>
                              <div className="text-sm text-purple-900 leading-relaxed whitespace-pre-wrap">{aiExplanations[q.id]}</div>
                            </div>
                          )}
                        </div>
                      )}

                      {/* Difficulty badge */}
                      <div className="mt-3 flex items-center gap-2 text-xs text-gray-400">
                        {q.difficulty && (
                          <span className={`px-2 py-1 rounded ${
                            q.difficulty === 'HARD' ? 'bg-red-100 text-red-600' :
                            q.difficulty === 'MEDIUM' ? 'bg-amber-100 text-amber-600' :
                            'bg-green-100 text-green-600'
                          }`}>
                            {q.difficulty}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}

                {/* Summary card at the bottom when all answered */}
                {answeredCount === totalQuestions && totalQuestions > 0 && (
                  <div className="bg-white rounded-xl border-2 border-indigo-200 p-6 text-center">
                    <div className="w-16 h-16 bg-indigo-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
                      <svg className="w-8 h-8 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-1">Chapter Complete!</h3>
                    <p className="text-gray-500 mb-4">
                      You answered {correctCount} of {totalQuestions} correctly ({Math.round((correctCount / totalQuestions) * 100)}%)
                    </p>
                    <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
                      <button
                        onClick={goToChapters}
                        className="px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors cursor-pointer"
                      >
                        Practice Another Chapter
                      </button>
                      <button
                        onClick={goToSubjects}
                        className="px-6 py-2.5 text-indigo-600 bg-indigo-50 text-sm font-medium rounded-lg hover:bg-indigo-100 transition-colors cursor-pointer"
                      >
                        Change Subject
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
