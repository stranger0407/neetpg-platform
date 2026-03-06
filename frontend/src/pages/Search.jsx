import { useState, useEffect } from 'react';
import api from '../api';

export default function Search() {
  const [keyword, setKeyword] = useState('');
  const [subjectId, setSubjectId] = useState('');
  const [difficulty, setDifficulty] = useState('');
  const [subjects, setSubjects] = useState([]);
  const [results, setResults] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  const optionLabels = ['A', 'B', 'C', 'D'];
  const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD'];

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

  const doSearch = async (pageNum = 0) => {
    if (!keyword.trim()) return;

    setLoading(true);
    setError('');
    setSearched(true);
    try {
      const params = new URLSearchParams();
      params.set('keyword', keyword.trim());
      params.set('page', pageNum);
      params.set('size', '20');
      if (subjectId) params.set('subjectId', subjectId);
      if (difficulty) params.set('difficulty', difficulty);

      const res = await api.get(`/search?${params.toString()}`);
      const data = res.data || {};
      setResults(data.questions || []);
      setTotalElements(data.totalElements || 0);
      setTotalPages(data.totalPages || 0);
      setPage(data.currentPage || 0);
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e?.preventDefault();
    setPage(0);
    doSearch(0);
  };

  const goToPage = (p) => {
    doSearch(p);
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
          <div className="flex flex-col gap-3">
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
              <button
                type="submit"
                disabled={loading || !keyword.trim()}
                className="px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer transition-colors"
              >
                {loading ? 'Searching...' : 'Search'}
              </button>
            </div>
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="sm:w-48">
                <select
                  value={subjectId}
                  onChange={(e) => setSubjectId(e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-gray-700 bg-white"
                >
                  <option value="">All Subjects</option>
                  {subjects.map((s) => (
                    <option key={s.id} value={s.id}>{s.name}</option>
                  ))}
                </select>
              </div>
              <div className="sm:w-40">
                <select
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-gray-700 bg-white"
                >
                  <option value="">All Difficulty</option>
                  <option value="EASY">Easy</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HARD">Hard</option>
                </select>
              </div>
            </div>
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
            <p className="text-sm text-gray-400 mt-1">Try different keywords or adjust filters.</p>
          </div>
        )}

        {results.length > 0 && (
          <div className="space-y-4">
            <p className="text-sm text-gray-500">{totalElements} result{totalElements !== 1 ? 's' : ''} found</p>
            {results.map((q) => {
              const isExpanded = expandedId === q.id;
              return (
                <div key={q.id} className="bg-white rounded-xl border border-gray-100 p-5 sm:p-6">
                  <p className="text-gray-900 leading-relaxed whitespace-pre-wrap mb-4">
                    {q.questionText}
                  </p>

                  <div className="space-y-2 mb-4">
                    {optionKeys.map((key, idx) => {
                      const isCorrect = optionLabels[idx] === q.correctAnswer;
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
                          <span className="text-sm text-gray-700">{q[key]}</span>
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
                    onClick={() => setExpandedId(isExpanded ? null : q.id)}
                    className="text-sm text-indigo-600 hover:text-indigo-800 font-medium cursor-pointer mb-3"
                  >
                    {isExpanded ? 'Hide Answer' : 'Show Answer'}
                  </button>

                  {isExpanded && q.explanation && (
                    <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                      <p className="text-xs font-semibold text-blue-700 mb-1">Explanation</p>
                      <p className="text-sm text-blue-900 leading-relaxed whitespace-pre-wrap">
                        {q.explanation}
                      </p>
                    </div>
                  )}

                  <div className="mt-3 flex items-center gap-3 text-xs text-gray-400">
                    {q.subjectName && (
                      <span className="px-2 py-1 bg-gray-100 rounded text-gray-600">
                        {q.subjectName}
                      </span>
                    )}
                    {q.chapterName && (
                      <span className="px-2 py-1 bg-gray-100 rounded text-gray-600">
                        {q.chapterName}
                      </span>
                    )}
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

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 pt-4">
                <button
                  onClick={() => goToPage(page - 1)}
                  disabled={page === 0}
                  className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                  Previous
                </button>
                <span className="text-sm text-gray-600">
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  onClick={() => goToPage(page + 1)}
                  disabled={page >= totalPages - 1}
                  className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                  Next
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
