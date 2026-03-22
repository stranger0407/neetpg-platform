import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';

export default function PracticeMode() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [revealed, setRevealed] = useState({});
  const [selected, setSelected] = useState({});
  const [bookmarked, setBookmarked] = useState({});

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const res = await api.get(`/practice/chapter/${chapterId}`);
        setData(res.data);
        const bm = {};
        (res.data.questions || []).forEach(q => {
          if (q.bookmarked) bm[q.id] = true;
        });
        setBookmarked(bm);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load questions.');
      } finally {
        setLoading(false);
      }
    };
    fetchQuestions();
  }, [chapterId]);

  const toggleReveal = (idx) => {
    setRevealed(prev => ({ ...prev, [idx]: !prev[idx] }));
  };

  const selectOption = (idx, label) => {
    if (revealed[idx]) return;
    setSelected(prev => ({ ...prev, [idx]: label }));
  };

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

  const revealedCount = Object.keys(revealed).filter(k => revealed[k]).length;

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading practice questions...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl p-8 border border-red-200 max-w-md text-center">
          <p className="text-red-600 font-medium">{error}</p>
          <button onClick={() => navigate(-1)} className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 cursor-pointer">
            Go Back
          </button>
        </div>
      </div>
    );
  }

  const questions = data?.questions || [];

  if (questions.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl p-8 border border-gray-200 max-w-md text-center">
          <p className="text-gray-600">No questions available for this chapter.</p>
          <button onClick={() => navigate(-1)} className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 cursor-pointer">
            Go Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Sticky Header */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-40 shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate(-1)}
              className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 cursor-pointer transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div>
              <h1 className="text-sm font-semibold text-gray-900">{data?.chapterName}</h1>
              <p className="text-xs text-gray-500">{data?.subjectName}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 text-sm">
              <span className="text-emerald-600 font-semibold">{revealedCount}</span>
              <span className="text-gray-400">/</span>
              <span className="text-gray-600 font-medium">{questions.length}</span>
              <span className="text-gray-400 text-xs ml-1">reviewed</span>
            </div>
            <div className="px-2.5 py-1 bg-emerald-50 text-emerald-700 text-xs font-medium rounded-full">
              Practice Mode
            </div>
          </div>
        </div>
        {/* Progress bar */}
        <div className="h-1 bg-gray-100">
          <div
            className="h-full bg-emerald-500 transition-all duration-500"
            style={{ width: `${questions.length > 0 ? (revealedCount / questions.length) * 100 : 0}%` }}
          />
        </div>
      </div>

      {/* Scrollable Questions */}
      <div className="max-w-4xl mx-auto px-4 py-6 space-y-6">
        {questions.map((q, idx) => {
          const isRevealed = revealed[idx];
          const userAnswer = selected[idx];
          const options = [
            { label: 'A', text: q.optionA },
            { label: 'B', text: q.optionB },
            { label: 'C', text: q.optionC },
            { label: 'D', text: q.optionD },
          ];

          return (
            <div
              key={q.id}
              id={`question-${idx}`}
              className="bg-white rounded-xl border border-gray-100 overflow-hidden transition-all hover:shadow-sm"
            >
              {/* Question Header */}
              <div className="px-6 pt-5 pb-3 flex items-start justify-between">
                <div className="flex items-center gap-2">
                  <span className="px-2.5 py-1 bg-gray-100 text-gray-700 text-xs font-semibold rounded-md">
                    Q{idx + 1}
                  </span>
                  {q.difficulty && (
                    <span className={`px-2.5 py-1 text-xs font-medium rounded-md ${
                      q.difficulty === 'EASY' ? 'bg-green-50 text-green-700' :
                      q.difficulty === 'HARD' ? 'bg-red-50 text-red-700' :
                      'bg-amber-50 text-amber-700'
                    }`}>
                      {q.difficulty}
                    </span>
                  )}
                </div>
                <button
                  onClick={() => toggleBookmark(q.id)}
                  className={`p-1.5 rounded-lg cursor-pointer transition-colors ${
                    bookmarked[q.id] ? 'text-amber-500 bg-amber-50' : 'text-gray-300 hover:text-gray-400 hover:bg-gray-50'
                  }`}
                >
                  <svg className="w-5 h-5" fill={bookmarked[q.id] ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                  </svg>
                </button>
              </div>

              {/* Question Text */}
              <div className="px-6 pb-4">
                <p className="text-gray-900 leading-relaxed">{q.questionText}</p>
              </div>

              {/* Options */}
              <div className="px-6 pb-4 space-y-2">
                {options.map((opt) => {
                  const isSelected = userAnswer === opt.label;
                  const isCorrect = opt.label === q.correctAnswer;

                  let cardClass = 'border-gray-200 hover:border-gray-300';
                  if (isRevealed) {
                    if (isCorrect) {
                      cardClass = 'border-green-500 bg-green-50 ring-1 ring-green-500';
                    } else if (isSelected && !isCorrect) {
                      cardClass = 'border-red-400 bg-red-50 ring-1 ring-red-400';
                    } else {
                      cardClass = 'border-gray-200 opacity-60';
                    }
                  } else if (isSelected) {
                    cardClass = 'border-indigo-500 bg-indigo-50 ring-1 ring-indigo-500';
                  }

                  return (
                    <button
                      key={opt.label}
                      onClick={() => selectOption(idx, opt.label)}
                      disabled={isRevealed}
                      className={`w-full text-left p-3 rounded-lg border-2 transition-all flex items-start gap-3 ${
                        isRevealed ? '' : 'cursor-pointer'
                      } ${cardClass}`}
                    >
                      <span className={`w-7 h-7 rounded-md flex items-center justify-center text-xs font-semibold shrink-0 ${
                        isRevealed && isCorrect ? 'bg-green-600 text-white' :
                        isRevealed && isSelected && !isCorrect ? 'bg-red-500 text-white' :
                        isSelected && !isRevealed ? 'bg-indigo-600 text-white' :
                        'bg-gray-100 text-gray-600'
                      }`}>
                        {isRevealed && isCorrect ? '✓' : isRevealed && isSelected && !isCorrect ? '✗' : opt.label}
                      </span>
                      <span className="text-sm text-gray-800 pt-0.5">{opt.text}</span>
                    </button>
                  );
                })}
              </div>

              {/* Show Answer / Explanation */}
              <div className="px-6 pb-5">
                {!isRevealed ? (
                  <button
                    onClick={() => toggleReveal(idx)}
                    className="w-full py-2.5 px-4 text-sm font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg hover:bg-emerald-100 transition-colors cursor-pointer"
                  >
                    Show Answer
                  </button>
                ) : (
                  <div className="mt-2 p-4 rounded-lg bg-blue-50 border border-blue-100">
                    <div className="flex items-center gap-2 mb-2">
                      <svg className="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span className="text-sm font-semibold text-blue-800">Explanation</span>
                    </div>
                    <p className="text-sm text-blue-900 leading-relaxed">{q.explanation}</p>
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {/* Bottom Spacer */}
        <div className="pb-8 text-center">
          <p className="text-gray-400 text-sm">— End of {questions.length} questions —</p>
          <button
            onClick={() => navigate(-1)}
            className="mt-4 px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 cursor-pointer transition-colors"
          >
            Back to Chapters
          </button>
        </div>
      </div>
    </div>
  );
}
