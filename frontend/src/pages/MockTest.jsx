import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

const MOCK_TEST_DURATION = 3.5 * 60 * 60; // 3.5 hours in seconds

export default function MockTest() {
  const navigate = useNavigate();
  const [phase, setPhase] = useState('start'); // start | quiz | submitting
  const [session, setSession] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState({});       // { index: 'A'|'B'|'C'|'D' }
  const [markedForReview, setMarkedForReview] = useState({}); // { index: true }
  const [bookmarked, setBookmarked] = useState({});
  const [timeLeft, setTimeLeft] = useState(MOCK_TEST_DURATION);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showNav, setShowNav] = useState(false);
  const timerRef = useRef(null);
  const answersRef = useRef(answers);
  const questionsRef = useRef(questions);
  const sessionRef = useRef(session);
  const phaseRef = useRef(phase);

  // Keep refs in sync
  useEffect(() => { answersRef.current = answers; }, [answers]);
  useEffect(() => { questionsRef.current = questions; }, [questions]);
  useEffect(() => { sessionRef.current = session; }, [session]);
  useEffect(() => { phaseRef.current = phase; }, [phase]);

  // Submit using refs to avoid stale closures
  const doSubmit = useCallback(async () => {
    if (phaseRef.current === 'submitting') return;
    setPhase('submitting');
    clearInterval(timerRef.current);

    try {
      const currentAnswers = answersRef.current;
      const currentQuestions = questionsRef.current;
      const currentSession = sessionRef.current;

      const formattedAnswers = currentQuestions.map((q, idx) => ({
        questionId: q.id,
        selectedAnswer: currentAnswers[idx] || null,
        timeTaken: 0,
      }));

      const sessionId = currentSession.sessionId;
      await api.post(`/quiz/${sessionId}/submit`, { answers: formattedAnswers });
      navigate(`/result/${sessionId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit mock test.');
      setPhase('quiz');
    }
  }, [navigate]);

  // Countdown timer
  useEffect(() => {
    if (phase !== 'quiz') return;
    timerRef.current = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          doSubmit();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current);
  }, [phase, doSubmit]);

  const startMockTest = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.post('/mock-test/start');
      setSession(res.data);
      setQuestions(res.data.questions || []);
      // Initialize bookmarked state from server data
      const bm = {};
      (res.data.questions || []).forEach(q => {
        if (q.bookmarked) bm[q.id] = true;
      });
      setBookmarked(bm);
      setPhase('quiz');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to start mock test.');
    } finally {
      setLoading(false);
    }
  };

  const selectAnswer = (label) => {
    setAnswers(prev => ({ ...prev, [currentIndex]: label }));
  };

  const clearAnswer = () => {
    setAnswers(prev => {
      const next = { ...prev };
      delete next[currentIndex];
      return next;
    });
  };

  const toggleReview = () => {
    setMarkedForReview(prev => {
      const next = { ...prev };
      if (next[currentIndex]) delete next[currentIndex];
      else next[currentIndex] = true;
      return next;
    });
  };

  const toggleBookmark = async (questionId) => {
    try {
      await api.post(`/bookmarks/${questionId}`);
      setBookmarked((prev) => {
        const next = { ...prev };
        if (next[questionId]) delete next[questionId];
        else next[questionId] = true;
        return next;
      });
    } catch { /* silent */ }
  };

  const formatTimeLeft = (seconds) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const answeredCount = Object.keys(answers).length;
  const reviewCount = Object.keys(markedForReview).length;
  const currentQuestion = questions[currentIndex];

  // Start screen
  if (phase === 'start') {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="bg-white rounded-2xl border border-gray-100 p-8 sm:p-12 max-w-lg w-full text-center">
          <div className="w-16 h-16 bg-indigo-100 rounded-2xl flex items-center justify-center mx-auto mb-6">
            <svg className="w-8 h-8 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Mock Test</h1>
          <p className="text-gray-500 mb-6">
            Simulate the real NEET PG exam experience with 200 questions and a 3.5-hour time limit.
          </p>

          <div className="grid grid-cols-3 gap-4 mb-8 text-center">
            <div className="p-3 bg-gray-50 rounded-xl">
              <p className="text-lg font-bold text-gray-900">200</p>
              <p className="text-xs text-gray-500">Questions</p>
            </div>
            <div className="p-3 bg-gray-50 rounded-xl">
              <p className="text-lg font-bold text-gray-900">3.5 hrs</p>
              <p className="text-xs text-gray-500">Duration</p>
            </div>
            <div className="p-3 bg-gray-50 rounded-xl">
              <p className="text-lg font-bold text-gray-900">800</p>
              <p className="text-xs text-gray-500">Max Marks</p>
            </div>
          </div>

          <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 mb-6 text-left">
            <p className="text-sm text-amber-800">
              <strong>Scoring:</strong> +4 for correct, -1 for incorrect, 0 for skipped.
              The test will auto-submit when time runs out.
            </p>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
              {error}
            </div>
          )}

          <button
            onClick={startMockTest}
            disabled={loading}
            className="w-full py-3 bg-indigo-600 text-white font-semibold rounded-xl hover:bg-indigo-700 disabled:opacity-50 cursor-pointer transition-colors"
          >
            {loading ? 'Starting...' : 'Start Mock Test'}
          </button>
        </div>
      </div>
    );
  }

  // Quiz screen
  if (!currentQuestion) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-500">No questions loaded.</p>
      </div>
    );
  }

  const optionFields = [
    { label: 'A', text: currentQuestion.optionA },
    { label: 'B', text: currentQuestion.optionB },
    { label: 'C', text: currentQuestion.optionC },
    { label: 'D', text: currentQuestion.optionD },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Bar */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-40">
        <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => setShowNav(!showNav)}
              className="lg:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 cursor-pointer"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <span className="text-sm font-medium text-gray-700">
              Q {currentIndex + 1} / {questions.length}
            </span>
            <span className="text-xs text-gray-400 hidden sm:inline">Mock Test</span>
          </div>

          <div className="flex items-center gap-4">
            {/* Timer */}
            <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-mono font-medium ${
              timeLeft < 600 ? 'bg-red-50 text-red-600' : 'bg-indigo-50 text-indigo-700'
            }`}>
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {formatTimeLeft(timeLeft)}
            </div>

            <button
              onClick={() => toggleBookmark(currentQuestion.id)}
              className={`p-2 rounded-lg cursor-pointer ${
                bookmarked[currentQuestion.id]
                  ? 'text-amber-500 bg-amber-50'
                  : 'text-gray-400 hover:bg-gray-100'
              }`}
            >
              <svg className="w-5 h-5" fill={bookmarked[currentQuestion.id] ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
            </button>

            <button
              onClick={() => {
                if (window.confirm('Are you sure you want to submit the mock test?')) doSubmit();
              }}
              disabled={phase === 'submitting'}
              className="px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700 disabled:opacity-50 cursor-pointer"
            >
              {phase === 'submitting' ? 'Submitting...' : 'Submit'}
            </button>
          </div>
        </div>

        <div className="h-1 bg-gray-100">
          <div
            className="h-full bg-indigo-600 transition-all duration-300"
            style={{ width: `${(answeredCount / questions.length) * 100}%` }}
          />
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-4 py-6 flex gap-6">
        {/* Desktop Navigator */}
        <div className="hidden lg:block w-64 shrink-0">
          <div className="bg-white rounded-xl border border-gray-100 p-4 sticky top-24">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Questions</h3>
            <div className="grid grid-cols-5 gap-2 max-h-96 overflow-y-auto">
              {questions.map((_, idx) => {
                let cls = 'bg-gray-100 text-gray-600';
                if (idx === currentIndex) cls = 'bg-indigo-600 text-white ring-2 ring-indigo-300';
                else if (markedForReview[idx]) cls = 'bg-amber-100 text-amber-700 ring-1 ring-amber-300';
                else if (answers[idx] !== undefined) cls = 'bg-green-100 text-green-700';
                return (
                  <button
                    key={idx}
                    onClick={() => { setCurrentIndex(idx); setShowNav(false); }}
                    className={`w-9 h-9 rounded-lg text-xs font-medium cursor-pointer ${cls}`}
                  >
                    {idx + 1}
                  </button>
                );
              })}
            </div>
            <div className="mt-3 pt-3 border-t border-gray-100 text-xs text-gray-500 space-y-1">
              <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-green-100 border border-green-300"></span> Answered: {answeredCount}</div>
              <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-amber-100 border border-amber-300"></span> Review: {reviewCount}</div>
              <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-gray-100 border border-gray-300"></span> Remaining: {questions.length - answeredCount}</div>
            </div>
          </div>
        </div>

        {/* Mobile Navigator */}
        {showNav && (
          <div className="fixed inset-0 z-50 lg:hidden">
            <div className="absolute inset-0 bg-black/30" onClick={() => setShowNav(false)} />
            <div className="absolute left-0 top-0 bottom-0 w-72 bg-white p-4 overflow-y-auto">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-gray-700">Questions</h3>
                <button onClick={() => setShowNav(false)} className="p-1 text-gray-400 cursor-pointer">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <div className="grid grid-cols-5 gap-2">
                {questions.map((_, idx) => {
                  let cls = 'bg-gray-100 text-gray-600';
                  if (idx === currentIndex) cls = 'bg-indigo-600 text-white';
                  else if (markedForReview[idx]) cls = 'bg-amber-100 text-amber-700';
                  else if (answers[idx] !== undefined) cls = 'bg-green-100 text-green-700';
                  return (
                    <button
                      key={idx}
                      onClick={() => { setCurrentIndex(idx); setShowNav(false); }}
                      className={`w-10 h-10 rounded-lg text-xs font-medium cursor-pointer ${cls}`}
                    >
                      {idx + 1}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* Question */}
        <div className="flex-1 min-w-0">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">{error}</div>
          )}

          <div className="bg-white rounded-xl border border-gray-100 p-6 sm:p-8">
            <div className="flex items-center gap-2 mb-4">
              <span className="inline-block px-2.5 py-1 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-md">
                Question {currentIndex + 1}
              </span>
              {currentQuestion.difficulty && (
                <span className={`inline-block px-2.5 py-1 text-xs font-medium rounded-md ${
                  currentQuestion.difficulty === 'EASY' ? 'bg-green-50 text-green-700' :
                  currentQuestion.difficulty === 'HARD' ? 'bg-red-50 text-red-700' :
                  'bg-yellow-50 text-yellow-700'
                }`}>{currentQuestion.difficulty}</span>
              )}
              {markedForReview[currentIndex] && (
                <span className="inline-block px-2.5 py-1 bg-amber-50 text-amber-700 text-xs font-medium rounded-md">Marked for Review</span>
              )}
            </div>
            <p className="text-gray-900 text-lg leading-relaxed whitespace-pre-wrap mb-8">
              {currentQuestion.questionText}
            </p>

            <div className="space-y-3">
              {optionFields.map(({ label, text }) => {
                const isSelected = answers[currentIndex] === label;
                let cardClass = 'border-gray-200 hover:border-indigo-300 hover:bg-indigo-50/50';
                if (isSelected) cardClass = 'border-indigo-500 bg-indigo-50 ring-1 ring-indigo-500';

                return (
                  <button
                    key={label}
                    onClick={() => selectAnswer(label)}
                    className={`w-full text-left p-4 rounded-xl border-2 transition-all cursor-pointer flex items-start gap-3 ${cardClass}`}
                  >
                    <span className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm font-semibold shrink-0 ${
                      isSelected ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600'
                    }`}>
                      {label}
                    </span>
                    <span className="text-gray-800 pt-1">{text}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Navigation */}
          <div className="flex items-center justify-between mt-6">
            <button
              onClick={() => currentIndex > 0 && setCurrentIndex(currentIndex - 1)}
              disabled={currentIndex === 0}
              className="px-5 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-40 cursor-pointer"
            >
              Previous
            </button>
            <div className="flex items-center gap-3">
              {answers[currentIndex] !== undefined && (
                <button
                  onClick={clearAnswer}
                  className="px-4 py-2.5 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer"
                >
                  Clear
                </button>
              )}
              <button
                onClick={toggleReview}
                className={`px-4 py-2.5 text-sm font-medium rounded-lg cursor-pointer ${
                  markedForReview[currentIndex]
                    ? 'text-amber-700 bg-amber-50 border border-amber-300'
                    : 'text-gray-600 bg-white border border-gray-200 hover:bg-gray-50'
                }`}
              >
                {markedForReview[currentIndex] ? 'Unmark Review' : 'Review Later'}
              </button>
              {currentIndex < questions.length - 1 ? (
                <button
                  onClick={() => setCurrentIndex(currentIndex + 1)}
                  className="px-5 py-2.5 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 cursor-pointer"
                >
                  Next
                </button>
              ) : (
                <button
                  onClick={() => {
                    if (window.confirm('Submit mock test?')) doSubmit();
                  }}
                  disabled={phase === 'submitting'}
                  className="px-5 py-2.5 text-sm font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 disabled:opacity-50 cursor-pointer"
                >
                  Finish & Submit
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
