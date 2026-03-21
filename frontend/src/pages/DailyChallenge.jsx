import { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../AuthContext';
import api from '../api';

const TIMER_MINUTES = 15;

export default function DailyChallenge() {
  const { user } = useAuth();
  const [phase, setPhase] = useState('loading'); // loading | pre | quiz | result
  const [challengeInfo, setChallengeInfo] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentIdx, setCurrentIdx] = useState(0);
  const [answers, setAnswers] = useState({});
  const [sessionId, setSessionId] = useState(null);
  const [result, setResult] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [timeLeft, setTimeLeft] = useState(TIMER_MINUTES * 60);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const timerRef = useRef(null);

  const fetchLeaderboard = useCallback(async () => {
    try {
      const res = await api.get('/daily-challenge/leaderboard');
      setLeaderboard(res.data);
    } catch {
      // Silent fail
    }
  }, []);

  const fetchChallenge = useCallback(async () => {
    try {
      const res = await api.get('/daily-challenge/today');
      const data = res.data;
      setChallengeInfo(data);

      if (data.alreadyAttempted) {
        setResult(data.result);
        setSessionId(data.sessionId);
        await fetchLeaderboard();
        setPhase('result');
      } else if (data.sessionId) {
        // Already started but not finished
        setQuestions(data.questions || []);
        setSessionId(data.sessionId);
        setPhase('quiz');
      } else {
        setQuestions(data.questions || []);
        setPhase('pre');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load daily challenge');
      setPhase('pre');
    }
  }, [fetchLeaderboard]);

  // Fetch today's challenge on mount
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchChallenge();
  }, [fetchChallenge]);

  const startChallenge = async () => {
    try {
      const res = await api.post('/daily-challenge/start');
      const data = res.data;
      setSessionId(data.sessionId);
      setQuestions(data.questions || []);
      setTimeLeft(TIMER_MINUTES * 60);
      setPhase('quiz');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to start challenge');
    }
  };

  const handleSubmit = useCallback(async () => {
    if (submitting) return;
    setSubmitting(true);
    clearInterval(timerRef.current);

    const answerList = questions.map(q => ({
      questionId: q.id,
      selectedAnswer: answers[q.id] || null,
      timeTaken: null,
    }));

    try {
      const res = await api.post('/daily-challenge/submit', {
        sessionId,
        answers: answerList,
      });
      setResult(res.data);
      await fetchLeaderboard();
      setPhase('result');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit');
      setSubmitting(false);
    }
  }, [submitting, questions, answers, sessionId, fetchLeaderboard]);

  // Timer countdown
  useEffect(() => {
    if (phase !== 'quiz') return;
    timerRef.current = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          handleSubmit();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current);
  }, [phase, handleSubmit]);

  const handleSelectAnswer = (questionId, answer) => {
    setAnswers(prev => ({ ...prev, [questionId]: answer }));
  };

  const formatTime = (s) => {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
  };

  const todayStr = new Date().toLocaleDateString('en-US', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
  });

  // --- LOADING ---
  if (phase === 'loading') {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading daily challenge...</p>
        </div>
      </div>
    );
  }

  // --- PRE-CHALLENGE ---
  if (phase === 'pre') {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-2xl mx-auto px-4 py-12">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-linear-to-br from-orange-400 to-red-500 rounded-2xl mb-4 shadow-lg">
              <span className="text-3xl">🔥</span>
            </div>
            <h1 className="text-3xl font-bold text-gray-900">Daily Challenge</h1>
            <p className="text-gray-500 mt-2">{todayStr}</p>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl text-red-600 text-sm text-center">
              {error}
            </div>
          )}

          {/* Rules Card */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-8 mb-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Challenge Rules</h2>
            <div className="space-y-3">
              {[
                { icon: '📝', text: `${challengeInfo?.questionCount || 10} questions from random topics` },
                { icon: '⏱️', text: `${challengeInfo?.timeLimitMinutes || 15} minutes time limit` },
                { icon: '✅', text: '+4 marks for correct answer' },
                { icon: '❌', text: '−1 mark for incorrect answer' },
                { icon: '⏭️', text: '0 marks for skipped questions' },
                { icon: '🏆', text: 'Compete with other students on the leaderboard' },
                { icon: '📅', text: 'New challenge every day — one attempt only!' },
              ].map((rule, i) => (
                <div key={i} className="flex items-center gap-3">
                  <span className="text-xl">{rule.icon}</span>
                  <span className="text-gray-700 text-sm">{rule.text}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Start Button */}
          <button
            onClick={startChallenge}
            className="w-full py-4 bg-linear-to-r from-orange-500 to-red-500 text-white font-semibold text-lg rounded-xl hover:from-orange-600 hover:to-red-600 transition-all shadow-lg hover:shadow-xl active:scale-[0.98] cursor-pointer"
          >
            🚀 Start Today's Challenge
          </button>

          {/* Countdown to next challenge */}
          <NextChallengeCountdown />
        </div>
      </div>
    );
  }

  // --- QUIZ IN PROGRESS ---
  if (phase === 'quiz') {
    const q = questions[currentIdx];
    const progress = ((currentIdx + 1) / questions.length) * 100;
    const answeredCount = Object.keys(answers).length;
    const isUrgent = timeLeft <= 60;

    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-3xl mx-auto px-4 py-6">
          {/* Timer & Progress Bar */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 mb-6">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-500">
                  Question {currentIdx + 1} of {questions.length}
                </span>
              </div>
              <div className={`flex items-center gap-2 px-3 py-1.5 rounded-lg font-mono text-sm font-bold ${
                isUrgent ? 'bg-red-50 text-red-600 animate-pulse' : 'bg-indigo-50 text-indigo-700'
              }`}>
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {formatTime(timeLeft)}
              </div>
            </div>
            <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-linear-to-r from-indigo-500 to-purple-500 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>

          {/* Question Card */}
          {q && (
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
              <div className="flex items-center gap-2 mb-4">
                <span className="px-2.5 py-0.5 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-full">
                  {q.subject}
                </span>
                <span className="px-2.5 py-0.5 bg-gray-100 text-gray-600 text-xs font-medium rounded-full">
                  {q.chapter}
                </span>
                <span className={`px-2.5 py-0.5 text-xs font-medium rounded-full ${
                  q.difficulty === 'EASY' ? 'bg-green-50 text-green-700' :
                  q.difficulty === 'HARD' ? 'bg-red-50 text-red-700' :
                  'bg-yellow-50 text-yellow-700'
                }`}>
                  {q.difficulty}
                </span>
              </div>

              <p className="text-gray-900 font-medium text-base leading-relaxed mb-6">
                {q.questionText}
              </p>

              <div className="space-y-3">
                {['A', 'B', 'C', 'D'].map(opt => {
                  const optionText = q[`option${opt}`];
                  const isSelected = answers[q.id] === opt;
                  return (
                    <button
                      key={opt}
                      onClick={() => handleSelectAnswer(q.id, opt)}
                      className={`w-full text-left p-4 rounded-xl border-2 transition-all cursor-pointer ${
                        isSelected
                          ? 'border-indigo-500 bg-indigo-50 text-indigo-900'
                          : 'border-gray-100 bg-gray-50 text-gray-700 hover:border-gray-200 hover:bg-gray-100'
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <span className={`flex items-center justify-center w-7 h-7 rounded-full text-sm font-bold shrink-0 ${
                          isSelected
                            ? 'bg-indigo-600 text-white'
                            : 'bg-white text-gray-500 border border-gray-200'
                        }`}>
                          {opt}
                        </span>
                        <span className="text-sm leading-relaxed">{optionText}</span>
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Navigation */}
          <div className="flex items-center justify-between gap-4">
            <button
              onClick={() => setCurrentIdx(Math.max(0, currentIdx - 1))}
              disabled={currentIdx === 0}
              className="px-5 py-2.5 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer transition-colors"
            >
              ← Previous
            </button>

            <div className="flex items-center gap-1.5 overflow-x-auto">
              {questions.map((qq, i) => (
                <button
                  key={i}
                  onClick={() => setCurrentIdx(i)}
                  className={`w-8 h-8 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    i === currentIdx
                      ? 'bg-indigo-600 text-white scale-110'
                      : answers[qq.id]
                        ? 'bg-green-100 text-green-700'
                        : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
            </div>

            {currentIdx < questions.length - 1 ? (
              <button
                onClick={() => setCurrentIdx(currentIdx + 1)}
                className="px-5 py-2.5 rounded-xl text-sm font-medium bg-indigo-600 text-white hover:bg-indigo-700 cursor-pointer transition-colors"
              >
                Next →
              </button>
            ) : (
              <button
                onClick={handleSubmit}
                disabled={submitting}
                className="px-5 py-2.5 rounded-xl text-sm font-medium bg-linear-to-r from-green-500 to-emerald-600 text-white hover:from-green-600 hover:to-emerald-700 disabled:opacity-50 cursor-pointer transition-all"
              >
                {submitting ? 'Submitting...' : `Submit (${answeredCount}/${questions.length})`}
              </button>
            )}
          </div>

          {error && (
            <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-xl text-red-600 text-sm text-center">
              {error}
            </div>
          )}
        </div>
      </div>
    );
  }

  // --- RESULT ---
  if (phase === 'result') {
    const r = result;
    const scorePercent = r ? Math.round((r.score / r.maxScore) * 100) : 0;
    const isGreat = scorePercent >= 70;
    const isGood = scorePercent >= 40;

    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-3xl mx-auto px-4 py-8">
          {/* Result Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-full mb-4 shadow-lg bg-linear-to-br from-orange-400 to-red-500">
              <span className="text-4xl">{isGreat ? '🏆' : isGood ? '👏' : '💪'}</span>
            </div>
            <h1 className="text-3xl font-bold text-gray-900">
              {isGreat ? 'Excellent!' : isGood ? 'Good Job!' : 'Keep Practicing!'}
            </h1>
            <p className="text-gray-500 mt-2">{todayStr} — Daily Challenge Complete</p>
          </div>

          {/* Score Card */}
          {r && (
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-8 mb-6">
              <div className="text-center mb-6">
                <div className="text-5xl font-bold text-gray-900">{r.score}</div>
                <div className="text-gray-500 text-sm mt-1">out of {r.maxScore} marks</div>
                <div className="w-full max-w-xs mx-auto h-3 bg-gray-100 rounded-full mt-4 overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-1000 ${
                      isGreat ? 'bg-linear-to-r from-green-400 to-emerald-500' :
                      isGood ? 'bg-linear-to-r from-yellow-400 to-orange-500' :
                      'bg-linear-to-r from-red-400 to-red-500'
                    }`}
                    style={{ width: `${Math.max(0, scorePercent)}%` }}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                <div className="text-center p-3 bg-green-50 rounded-xl">
                  <p className="text-2xl font-bold text-green-700">{r.correct}</p>
                  <p className="text-xs text-green-600 mt-0.5">Correct</p>
                </div>
                <div className="text-center p-3 bg-red-50 rounded-xl">
                  <p className="text-2xl font-bold text-red-700">{r.incorrect}</p>
                  <p className="text-xs text-red-600 mt-0.5">Incorrect</p>
                </div>
                <div className="text-center p-3 bg-gray-50 rounded-xl">
                  <p className="text-2xl font-bold text-gray-700">{r.skipped}</p>
                  <p className="text-xs text-gray-500 mt-0.5">Skipped</p>
                </div>
                <div className="text-center p-3 bg-indigo-50 rounded-xl">
                  <p className="text-2xl font-bold text-indigo-700">{r.accuracy}%</p>
                  <p className="text-xs text-indigo-600 mt-0.5">Accuracy</p>
                </div>
              </div>

              {/* Rank */}
              <div className="mt-6 p-4 bg-linear-to-r from-amber-50 to-yellow-50 rounded-xl text-center border border-amber-100">
                <p className="text-sm text-amber-700 font-medium">Your Rank</p>
                <p className="text-3xl font-bold text-amber-800 mt-1">
                  #{r.rank} <span className="text-sm font-normal text-amber-600">of {r.totalParticipants} participants</span>
                </p>
              </div>
            </div>
          )}

          {/* Leaderboard */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <span>🏅</span> Today's Leaderboard
            </h2>
            {leaderboard.length > 0 ? (
              <div className="space-y-2">
                {leaderboard.map((entry) => {
                  const isMe = entry.userId === (user?.id);
                  const medals = ['🥇', '🥈', '🥉'];
                  return (
                    <div
                      key={entry.rank}
                      className={`flex items-center justify-between p-3 rounded-xl transition-colors ${
                        isMe ? 'bg-indigo-50 border border-indigo-100' : 'bg-gray-50 hover:bg-gray-100'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <span className="text-lg w-8 text-center">
                          {entry.rank <= 3 ? medals[entry.rank - 1] : (
                            <span className="text-sm font-bold text-gray-400">{entry.rank}</span>
                          )}
                        </span>
                        <div>
                          <p className={`text-sm font-medium ${isMe ? 'text-indigo-700' : 'text-gray-900'}`}>
                            {entry.userName} {isMe && <span className="text-xs text-indigo-500">(You)</span>}
                          </p>
                          <p className="text-xs text-gray-500">
                            {entry.correct}/{entry.totalQuestions} correct
                          </p>
                        </div>
                      </div>
                      <span className={`text-sm font-bold ${isMe ? 'text-indigo-700' : 'text-gray-700'}`}>
                        {entry.score} pts
                      </span>
                    </div>
                  );
                })}
              </div>
            ) : (
              <p className="text-gray-400 text-sm text-center py-4">
                No participants yet. Be the first! 🚀
              </p>
            )}
          </div>

          {/* Next Challenge */}
          <NextChallengeCountdown />
        </div>
      </div>
    );
  }

  return null;
}

// --- Countdown to next day's challenge ---
function NextChallengeCountdown() {
  const [timeToNext, setTimeToNext] = useState('');

  useEffect(() => {
    const update = () => {
      const now = new Date();
      const tomorrow = new Date(now);
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(0, 0, 0, 0);
      const diff = tomorrow - now;
      const h = Math.floor(diff / 3600000);
      const m = Math.floor((diff % 3600000) / 60000);
      const s = Math.floor((diff % 60000) / 1000);
      setTimeToNext(`${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`);
    };
    update();
    const iv = setInterval(update, 1000);
    return () => clearInterval(iv);
  }, []);

  return (
    <div className="mt-6 text-center">
      <p className="text-sm text-gray-400">Next challenge in</p>
      <p className="text-2xl font-mono font-bold text-gray-600 mt-1">{timeToNext}</p>
    </div>
  );
}
