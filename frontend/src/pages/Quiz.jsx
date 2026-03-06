import { useState, useEffect, useCallback, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import api from '../api';

export default function Quiz() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [sessionId, setSessionId] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [timers, setTimers] = useState({});
  const [bookmarked, setBookmarked] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [showNav, setShowNav] = useState(false);
  const timerInterval = useRef(null);

  useEffect(() => {
    const startQuiz = async () => {
      try {
        const body = {};
        const chapterId = searchParams.get('chapterId');
        const quizType = searchParams.get('quizType');
        const questionCount = searchParams.get('questionCount');
        const difficulty = searchParams.get('difficulty');
        const isMock = searchParams.get('mock');

        if (chapterId) body.chapterId = parseInt(chapterId, 10);
        if (quizType) body.quizType = quizType.toUpperCase();
        if (questionCount) body.questionCount = parseInt(questionCount, 10);
        if (difficulty) body.difficulty = difficulty.toUpperCase();

        const endpoint = isMock ? '/mock-test/start' : '/quiz/start';
        const res = await api.post(endpoint, body);
        setSessionId(res.data.sessionId);
        setQuestions(res.data.questions || []);
        const bm = {};
        (res.data.questions || []).forEach(q => {
          if (q.bookmarked) bm[q.id] = true;
        });
        setBookmarked(bm);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to start quiz.');
      } finally {
        setLoading(false);
      }
    };
    startQuiz();
  }, [searchParams]);

  // Per-question timer
  useEffect(() => {
    if (!questions.length) return;
    clearInterval(timerInterval.current);
    timerInterval.current = setInterval(() => {
      setTimers(prev => ({ ...prev, [currentIndex]: (prev[currentIndex] || 0) + 1 }));
    }, 1000);
    return () => clearInterval(timerInterval.current);
  }, [currentIndex, questions.length]);

  const currentQuestion = questions[currentIndex];
  const options = currentQuestion ? [
    { label: 'A', text: currentQuestion.optionA },
    { label: 'B', text: currentQuestion.optionB },
    { label: 'C', text: currentQuestion.optionC },
    { label: 'D', text: currentQuestion.optionD },
  ] : [];

  const selectAnswer = (label) => {
    setAnswers(prev => ({ ...prev, [currentIndex]: label }));
  };

  const goToQuestion = (index) => { setCurrentIndex(index); setShowNav(false); };
  const goNext = () => { if (currentIndex < questions.length - 1) setCurrentIndex(currentIndex + 1); };
  const goPrev = () => { if (currentIndex > 0) setCurrentIndex(currentIndex - 1); };

  const toggleBookmark = async (questionId) => {
    try {
      await api.post(`/bookmarks/${questionId}`);
      setBookmarked(prev => {
        const copy = { ...prev };
        if (copy[questionId]) delete copy[questionId];
        else copy[questionId] = true;
        return copy;
      });
    } catch { /* silent */ }
  };

  const submitQuiz = useCallback(async () => {
    if (submitting) return;
    setSubmitting(true);
    try {
      const formattedAnswers = questions.map((q, idx) => ({
        questionId: q.id,
        selectedAnswer: answers[idx] || null,
        timeTaken: timers[idx] || 0,
      }));
      await api.post(`/quiz/${sessionId}/submit`, { answers: formattedAnswers });
      navigate(`/result/${sessionId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit quiz.');
      setSubmitting(false);
    }
  }, [answers, timers, questions, sessionId, navigate, submitting]);

  const formatTime = (seconds) => {
    const s = seconds || 0;
    return `${Math.floor(s / 60)}:${(s % 60).toString().padStart(2, '0')}`;
  };

  const answeredCount = Object.keys(answers).length;

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Preparing your quiz...</p>
        </div>
      </div>
    );
  }

  if (error && !sessionId) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl p-8 border border-red-200 max-w-md text-center">
          <p className="text-red-600 font-medium">{error}</p>
          <button onClick={() => navigate(-1)} className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 cursor-pointer">Go Back</button>
        </div>
      </div>
    );
  }

  if (!currentQuestion) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl p-8 border border-gray-200 max-w-md text-center">
          <p className="text-gray-600">No questions available.</p>
          <button onClick={() => navigate('/subjects')} className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 cursor-pointer">Browse Subjects</button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Bar */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-40">
        <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button onClick={() => setShowNav(!showNav)} className="lg:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 cursor-pointer">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>
            </button>
            <span className="text-sm font-medium text-gray-700">Q {currentIndex + 1} / {questions.length}</span>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1.5 text-sm text-gray-600">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              {formatTime(timers[currentIndex])}
            </div>
            <button onClick={() => toggleBookmark(currentQuestion.id)}
              className={`p-2 rounded-lg cursor-pointer transition-colors ${bookmarked[currentQuestion.id] ? 'text-amber-500 bg-amber-50' : 'text-gray-400 hover:bg-gray-100'}`}>
              <svg className="w-5 h-5" fill={bookmarked[currentQuestion.id] ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" /></svg>
            </button>
            <button onClick={() => { if (window.confirm('Submit quiz?')) submitQuiz(); }} disabled={submitting}
              className="px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700 disabled:opacity-50 cursor-pointer">
              {submitting ? 'Submitting...' : 'Submit'}
            </button>
          </div>
        </div>
        <div className="h-1 bg-gray-100">
          <div className="h-full bg-indigo-600 transition-all duration-300" style={{ width: `${(answeredCount / questions.length) * 100}%` }} />
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-4 py-6 flex gap-6">
        {/* Desktop Navigator */}
        <div className="hidden lg:block w-64 shrink-0">
          <div className="bg-white rounded-xl border border-gray-100 p-4 sticky top-24">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Question Navigator</h3>
            <div className="grid grid-cols-5 gap-2">
              {questions.map((_, idx) => {
                let c = 'bg-gray-100 text-gray-600';
                if (idx === currentIndex) c = 'bg-indigo-600 text-white ring-2 ring-indigo-300';
                else if (answers[idx]) c = 'bg-green-100 text-green-700';
                return <button key={idx} onClick={() => goToQuestion(idx)} className={`w-9 h-9 rounded-lg text-xs font-medium cursor-pointer ${c}`}>{idx + 1}</button>;
              })}
            </div>
            <div className="mt-4 pt-4 border-t border-gray-100 space-y-1.5 text-xs text-gray-500">
              <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-green-100 inline-block" /> Answered: {answeredCount}</div>
              <div className="flex items-center gap-2"><span className="w-3 h-3 rounded bg-gray-100 inline-block" /> Remaining: {questions.length - answeredCount}</div>
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
                <button onClick={() => setShowNav(false)} className="p-1 text-gray-400 cursor-pointer"><svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg></button>
              </div>
              <div className="grid grid-cols-5 gap-2">
                {questions.map((_, idx) => {
                  let c = 'bg-gray-100 text-gray-600';
                  if (idx === currentIndex) c = 'bg-indigo-600 text-white';
                  else if (answers[idx]) c = 'bg-green-100 text-green-700';
                  return <button key={idx} onClick={() => goToQuestion(idx)} className={`w-10 h-10 rounded-lg text-xs font-medium cursor-pointer ${c}`}>{idx + 1}</button>;
                })}
              </div>
            </div>
          </div>
        )}

        {/* Question */}
        <div className="flex-1 min-w-0">
          {error && <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">{error}</div>}
          <div className="bg-white rounded-xl border border-gray-100 p-6 sm:p-8">
            <div className="mb-8">
              <div className="flex items-center gap-2 mb-4">
                <span className="px-2.5 py-1 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-md">Question {currentIndex + 1}</span>
                {currentQuestion.difficulty && <span className={`px-2.5 py-1 text-xs font-medium rounded-md ${currentQuestion.difficulty === 'EASY' ? 'bg-green-50 text-green-700' : currentQuestion.difficulty === 'HARD' ? 'bg-red-50 text-red-700' : 'bg-amber-50 text-amber-700'}`}>{currentQuestion.difficulty}</span>}
              </div>
              <p className="text-gray-900 text-lg leading-relaxed">{currentQuestion.questionText}</p>
            </div>
            <div className="space-y-3">
              {options.map((opt) => {
                const isSelected = answers[currentIndex] === opt.label;
                let cardClass = 'border-gray-200 hover:border-indigo-300 hover:bg-indigo-50/50';
                if (isSelected) cardClass = 'border-indigo-500 bg-indigo-50 ring-1 ring-indigo-500';
                return (
                  <button key={opt.label} onClick={() => selectAnswer(opt.label)}
                    className={`w-full text-left p-4 rounded-xl border-2 transition-all cursor-pointer flex items-start gap-3 ${cardClass}`}>
                    <span className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm font-semibold shrink-0 ${isSelected ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600'}`}>{opt.label}</span>
                    <span className="text-gray-800 pt-1">{opt.text}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="flex items-center justify-between mt-6">
            <button onClick={goPrev} disabled={currentIndex === 0} className="px-5 py-2.5 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-40 cursor-pointer">Previous</button>
            <div className="flex items-center gap-3">
              <button onClick={goNext} disabled={currentIndex === questions.length - 1} className="px-5 py-2.5 text-sm font-medium text-gray-500 hover:text-gray-700 cursor-pointer">Skip</button>
              {currentIndex === questions.length - 1 ? (
                <button onClick={() => { if (window.confirm('Submit quiz?')) submitQuiz(); }} disabled={submitting}
                  className="px-5 py-2.5 text-sm font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 disabled:opacity-50 cursor-pointer">
                  {submitting ? 'Submitting...' : 'Finish & Submit'}
                </button>
              ) : (
                <button onClick={goNext} className="px-5 py-2.5 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 cursor-pointer">Next</button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
