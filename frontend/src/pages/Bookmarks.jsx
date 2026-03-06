import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

export default function Bookmarks() {
  const navigate = useNavigate();
  const [bookmarks, setBookmarks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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
      await api.delete(`/bookmarks/${questionId}`);
      setBookmarks((prev) => prev.filter((b) => (b.questionId || b._id || b.id) !== questionId));
    } catch {
      // silently fail
    }
  };

  const practiceBookmarks = () => {
    const ids = bookmarks.map((b) => b.questionId || b._id || b.id).join(',');
    const params = new URLSearchParams({
      quizType: 'bookmark',
      questionIds: ids,
    });
    navigate(`/quiz?${params.toString()}`);
  };

  const optionLabels = ['A', 'B', 'C', 'D'];

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
          {bookmarks.length > 0 && (
            <button
              onClick={practiceBookmarks}
              className="px-5 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors cursor-pointer"
            >
              Practice All
            </button>
          )}
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
              const q = bookmark.question || bookmark;
              const questionId = bookmark.questionId || q._id || q.id;

              return (
                <div key={questionId} className="bg-white rounded-xl border border-gray-100 p-5 sm:p-6">
                  <div className="flex items-start justify-between gap-4 mb-4">
                    <p className="text-gray-900 leading-relaxed whitespace-pre-wrap">
                      {q.questionText || q.question}
                    </p>
                    <button
                      onClick={() => removeBookmark(questionId)}
                      className="shrink-0 p-1.5 text-amber-500 hover:text-red-500 rounded-lg cursor-pointer"
                      title="Remove bookmark"
                    >
                      <svg className="w-5 h-5" fill="currentColor" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                      </svg>
                    </button>
                  </div>

                  {q.options && (
                    <div className="space-y-2 mb-4">
                      {q.options.map((opt, idx) => {
                        const optText = typeof opt === 'string' ? opt : opt.text || opt.label;
                        const isCorrect = idx === (q.correctOption ?? q.correct ?? q.correctAnswer);
                        return (
                          <div
                            key={idx}
                            className={`flex items-center gap-3 p-2.5 rounded-lg border ${
                              isCorrect ? 'border-green-300 bg-green-50' : 'border-gray-100 bg-gray-50'
                            }`}
                          >
                            <span className="text-xs font-semibold w-6 text-center text-gray-500">
                              {optionLabels[idx]}
                            </span>
                            <span className="text-sm text-gray-700">{optText}</span>
                            {isCorrect && (
                              <svg className="w-4 h-4 text-green-600 ml-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                              </svg>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  )}

                  {(q.explanation || q.explanationText) && (
                    <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                      <p className="text-xs font-semibold text-blue-700 mb-1">Explanation</p>
                      <p className="text-sm text-blue-900 leading-relaxed whitespace-pre-wrap">
                        {q.explanation || q.explanationText}
                      </p>
                    </div>
                  )}

                  {(q.subject || q.subjectName || q.chapter || q.chapterName) && (
                    <div className="mt-3 flex items-center gap-2 text-xs text-gray-400">
                      {(q.subject || q.subjectName) && <span>{q.subject || q.subjectName}</span>}
                      {(q.chapter || q.chapterName) && (
                        <>
                          <span>/</span>
                          <span>{q.chapter || q.chapterName}</span>
                        </>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
