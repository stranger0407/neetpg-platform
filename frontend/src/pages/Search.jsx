import { useState, useEffect } from 'react';
import api from '../api';

export default function Search() {
  const [keyword, setKeyword] = useState('');
  const [subjectId, setSubjectId] = useState('');
  const [subjects, setSubjects] = useState([]);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState('');

  const optionLabels = ['A', 'B', 'C', 'D'];

  // Load subjects for filter dropdown
  useEffect(() => {
    const fetchSubjects = async () => {
      try {
        const res = await api.get('/subjects');
        setSubjects(res.data || []);
      } catch {
        // silently fail
      }
    };
    fetchSubjects();
  }, []);

  const handleSearch = async (e) => {
    e?.preventDefault();
    if (!keyword.trim()) return;

    setLoading(true);
    setError('');
    setSearched(true);
    try {
      const params = new URLSearchParams();
      params.set('keyword', keyword.trim());
      if (subjectId) params.set('subjectId', subjectId);

      const res = await api.get(`/search?${params.toString()}`);
      setResults(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Search Questions</h1>
          <p className="mt-1 text-gray-500">Find specific questions by keywords or topics.</p>
        </div>

        {/* Search Form */}
        <form onSubmit={handleSearch} className="bg-white rounded-xl border border-gray-100 p-5 mb-6">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Search by keyword, topic, or question text..."
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent placeholder-gray-400"
              />
            </div>
            <div className="sm:w-48">
              <select
                value={subjectId}
                onChange={(e) => setSubjectId(e.target.value)}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-gray-700 bg-white"
              >
                <option value="">All Subjects</option>
                {subjects.map((s) => (
                  <option key={s._id || s.id} value={s._id || s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <button
              type="submit"
              disabled={loading || !keyword.trim()}
              className="px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer transition-colors"
            >
              {loading ? 'Searching...' : 'Search'}
            </button>
          </div>
        </form>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Results */}
        {searched && !loading && results.length === 0 && (
          <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
            <p className="text-gray-500">No questions found matching your search.</p>
            <p className="text-sm text-gray-400 mt-1">Try different keywords or remove the subject filter.</p>
          </div>
        )}

        {results.length > 0 && (
          <div className="space-y-4">
            <p className="text-sm text-gray-500">{results.length} result{results.length !== 1 ? 's' : ''} found</p>
            {results.map((q, idx) => {
              const questionId = q._id || q.id;
              return (
                <div key={questionId || idx} className="bg-white rounded-xl border border-gray-100 p-5 sm:p-6">
                  <p className="text-gray-900 leading-relaxed whitespace-pre-wrap mb-4">
                    {q.questionText || q.question}
                  </p>

                  {q.options && (
                    <div className="space-y-2 mb-4">
                      {q.options.map((opt, optIdx) => {
                        const optText = typeof opt === 'string' ? opt : opt.text || opt.label;
                        const isCorrect = optIdx === (q.correctOption ?? q.correct ?? q.correctAnswer);
                        return (
                          <div
                            key={optIdx}
                            className={`flex items-center gap-3 p-2.5 rounded-lg border ${
                              isCorrect ? 'border-green-300 bg-green-50' : 'border-gray-100 bg-gray-50'
                            }`}
                          >
                            <span className="text-xs font-semibold w-6 text-center text-gray-500">
                              {optionLabels[optIdx]}
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
                    <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg mb-3">
                      <p className="text-xs font-semibold text-blue-700 mb-1">Explanation</p>
                      <p className="text-sm text-blue-900 leading-relaxed whitespace-pre-wrap">
                        {q.explanation || q.explanationText}
                      </p>
                    </div>
                  )}

                  <div className="flex items-center gap-3 text-xs text-gray-400">
                    {(q.subject || q.subjectName) && (
                      <span className="px-2 py-1 bg-gray-100 rounded text-gray-600">
                        {q.subject || q.subjectName}
                      </span>
                    )}
                    {(q.chapter || q.chapterName) && (
                      <span className="px-2 py-1 bg-gray-100 rounded text-gray-600">
                        {q.chapter || q.chapterName}
                      </span>
                    )}
                    {q.difficulty && (
                      <span className={`px-2 py-1 rounded ${
                        q.difficulty === 'Hard' ? 'bg-red-100 text-red-600' :
                        q.difficulty === 'Medium' ? 'bg-amber-100 text-amber-600' :
                        'bg-green-100 text-green-600'
                      }`}>
                        {q.difficulty}
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
