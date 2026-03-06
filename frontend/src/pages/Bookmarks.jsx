import { useState, useEffect } from 'react';
import api from '../api';

export default function Bookmarks() {
  const [bookmarks, setBookmarks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedIds, setExpandedIds] = useState(new Set());
  const [aiExplanations, setAiExplanations] = useState({});
  const [aiLoading, setAiLoading] = useState({});
  const [highlights, setHighlights] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('neetpg_highlights') || '{}');
    } catch {
      return {};
    }
  });

  const optionLabels = ['A', 'B', 'C', 'D'];
  const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD'];

  useEffect(() => {
    const fetchBookmarks = async () => {
      try {
        const res = await api.get('/bookmarks');
        setBookmarks(res.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load bookmarks.');
      } finally {
        setLoading(false);
      }
    };
    fetchBookmarks();
  }, []);

  const removeBookmark = async (questionId) => {
    try {
      await api.post(`/bookmarks/${questionId}`);
      setBookmarks((prev) => prev.filter((b) => b.questionId !== questionId));
    } catch {
      // silently fail
    }
  };

  const toggleExpand = (id) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleHighlight = (questionId) => {
    const sel = window.getSelection();
    const text = sel?.toString().trim();
    if (!text) return;

    setHighlights((prev) => {
      const qHighlights = prev[questionId] || [];
      if (qHighlights.includes(text)) return prev;
      const next = { ...prev, [questionId]: [...qHighlights, text] };
      localStorage.setItem('neetpg_highlights', JSON.stringify(next));
      return next;
    });
    sel.removeAllRanges();
  };

  const fetchAiExplanation = async (questionId) => {
    if (aiExplanations[questionId] || aiLoading[questionId]) return;
    setAiLoading(prev => ({ ...prev, [questionId]: true }));
    try {
      const res = await api.get(`/ai/explain/${questionId}`);
      setAiExplanations(prev => ({ ...prev, [questionId]: res.data.detailedExplanation }));
    } catch {
      setAiExplanations(prev => ({ ...prev, [questionId]: 'Failed to load AI explanation. Please try again.' }));
    } finally {
      setAiLoading(prev => ({ ...prev, [questionId]: false }));
    }
  };

  const removeHighlight = (questionId, text) => {
    setHighlights((prev) => {
      const qHighlights = (prev[questionId] || []).filter((h) => h !== text);
      const next = { ...prev };
      if (qHighlights.length === 0) delete next[questionId];
      else next[questionId] = qHighlights;
      localStorage.setItem('neetpg_highlights', JSON.stringify(next));
      return next;
    });
  };

  const renderHighlighted = (text, questionId) => {
    const qHighlights = highlights[questionId] || [];
    if (qHighlights.length === 0) return text;

    let result = text;
    const parts = [];
    let lastIndex = 0;

    // Find all highlight positions
    const positions = [];
    for (const h of qHighlights) {
      let idx = result.indexOf(h);
      while (idx !== -1) {
        positions.push({ start: idx, end: idx + h.length, text: h });
        idx = result.indexOf(h, idx + 1);
      }
    }
    positions.sort((a, b) => a.start - b.start);

    // Build highlighted spans
    for (const pos of positions) {
      if (pos.start < lastIndex) continue;
      if (pos.start > lastIndex) {
        parts.push(<span key={lastIndex}>{result.slice(lastIndex, pos.start)}</span>);
      }
      parts.push(
        <mark key={pos.start} className="bg-yellow-200 px-0.5 rounded">
          {result.slice(pos.start, pos.end)}
        </mark>
      );
      lastIndex = pos.end;
    }
    if (lastIndex < result.length) {
      parts.push(<span key={lastIndex}>{result.slice(lastIndex)}</span>);
    }
    return parts.length > 0 ? parts : text;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading bookmarks...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl p-8 border border-red-200 max-w-md text-center">
          <p className="text-red-600 font-medium">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 cursor-pointer"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Bookmarks</h1>
            <p className="mt-1 text-gray-500">
              {bookmarks.length} bookmarked question{bookmarks.length !== 1 ? 's' : ''}
            </p>
          </div>
        </div>

        <div className="mb-4 p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-700">
          Select text in explanations to highlight it. Highlights are saved locally.
        </div>

        {bookmarks.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
            </div>
            <p className="text-gray-500 mb-2">No bookmarked questions yet.</p>
            <p className="text-sm text-gray-400">Bookmark questions during quizzes to review them later.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {bookmarks.map((bookmark) => {
              const qId = bookmark.questionId;
              const isExpanded = expandedIds.has(qId);
              const qHighlights = highlights[qId] || [];

              return (
                <div key={qId} className="bg-white rounded-xl border border-gray-100 p-5 sm:p-6">
                  <div className="flex items-start justify-between gap-4 mb-4">
                    <p className="text-gray-900 leading-relaxed whitespace-pre-wrap">
                      {bookmark.questionText}
                    </p>
                    <button
                      onClick={() => removeBookmark(qId)}
                      className="shrink-0 p-1.5 text-amber-500 hover:text-red-500 rounded-lg cursor-pointer"
                      title="Remove bookmark"
                    >
                      <svg className="w-5 h-5" fill="currentColor" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                      </svg>
                    </button>
                  </div>

                  <div className="space-y-2 mb-4">
                    {optionKeys.map((key, idx) => {
                      const isCorrect = optionLabels[idx] === bookmark.correctAnswer;
                      return (
                        <div
                          key={key}
                          className={`flex items-center gap-3 p-2.5 rounded-lg border ${
                            isExpanded && isCorrect ? 'border-green-300 bg-green-50' :
                            'border-gray-100 bg-gray-50'
                          }`}
                        >
                          <span className="text-xs font-semibold w-6 text-center text-gray-500">
                            {optionLabels[idx]}
                          </span>
                          <span className="text-sm text-gray-700">{bookmark[key]}</span>
                          {isExpanded && isCorrect && (
                            <svg className="w-4 h-4 text-green-600 ml-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                          )}
                        </div>
                      );
                    })}
                  </div>

                  <button
                    onClick={() => toggleExpand(qId)}
                    className="text-sm text-indigo-600 hover:text-indigo-800 font-medium cursor-pointer"
                  >
                    {isExpanded ? 'Hide Answer' : 'Show Answer'}
                  </button>

                  {isExpanded && bookmark.explanation && (
                    <div
                      className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg"
                      onMouseUp={() => handleHighlight(qId)}
                    >
                      <p className="text-xs font-semibold text-blue-700 mb-1">Explanation</p>
                      <p className="text-sm text-blue-900 leading-relaxed whitespace-pre-wrap select-text">
                        {renderHighlighted(bookmark.explanation, qId)}
                      </p>
                    </div>
                  )}

                  {isExpanded && (
                    <div className="mt-3">
                      {!aiExplanations[qId] ? (
                        <button
                          onClick={() => fetchAiExplanation(qId)}
                          disabled={aiLoading[qId]}
                          className="px-4 py-2 text-sm font-medium text-indigo-600 bg-indigo-50 border border-indigo-200 rounded-lg hover:bg-indigo-100 disabled:opacity-50 cursor-pointer transition-colors"
                        >
                          {aiLoading[qId] ? (
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
                          <div className="text-sm text-purple-900 leading-relaxed whitespace-pre-wrap">{aiExplanations[qId]}</div>
                        </div>
                      )}
                    </div>
                  )}

                  {/* Show highlights for this question */}
                  {qHighlights.length > 0 && (
                    <div className="mt-3">
                      <p className="text-xs font-semibold text-gray-500 mb-1">Your Highlights</p>
                      <div className="flex flex-wrap gap-2">
                        {qHighlights.map((h, i) => (
                          <span
                            key={i}
                            className="inline-flex items-center gap-1 px-2 py-1 bg-yellow-100 border border-yellow-300 rounded text-xs text-yellow-800"
                          >
                            {h.length > 40 ? h.slice(0, 40) + '...' : h}
                            <button
                              onClick={() => removeHighlight(qId, h)}
                              className="text-yellow-600 hover:text-red-500 cursor-pointer font-bold"
                            >
                              x
                            </button>
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  <div className="mt-3 flex items-center gap-2 text-xs text-gray-400">
                    {bookmark.subjectName && <span className="px-2 py-1 bg-gray-100 rounded text-gray-600">{bookmark.subjectName}</span>}
                    {bookmark.chapterName && <span className="px-2 py-1 bg-gray-100 rounded text-gray-600">{bookmark.chapterName}</span>}
                    {bookmark.difficulty && (
                      <span className={`px-2 py-1 rounded ${
                        bookmark.difficulty === 'HARD' ? 'bg-red-100 text-red-600' :
                        bookmark.difficulty === 'MEDIUM' ? 'bg-amber-100 text-amber-600' :
                        'bg-green-100 text-green-600'
                      }`}>
                        {bookmark.difficulty}
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
