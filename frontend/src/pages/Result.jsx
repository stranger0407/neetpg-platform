import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api';

export default function Result() {
  const { sessionId } = useParams();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookmarked, setBookmarked] = useState({});

  useEffect(() => {
    const fetchResult = async () => {
      try {
        const res = await api.get(`/quiz/${sessionId}/result`);
        setResult(res.data);
        if (res.data.questionDetails) {
          const bm = {};
          res.data.questionDetails.forEach(q => {
            if (q.bookmarked) bm[q.id] = true;
          });
          setBookmarked(bm);
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load results.');
      } finally {
        setLoading(false);
      }
    };
    fetchResult();
  }, [sessionId]);

  const toggleBookmark = async (questionId) => {
    try {
      const res = await api.post(`/bookmarks/${questionId}`);
      setBookmarked(prev => {
        const copy = { ...prev };
        if (res.data.bookmarked) copy[questionId] = true;
        else delete copy[questionId];
        return copy;
      });
    } catch { /* silent */ }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading results...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl p-8 border border-red-200 max-w-md text-center">
          <p className="text-red-600 font-medium">{error}</p>
          <Link to="/dashboard" className="mt-4 inline-block px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">Go to Dashboard</Link>
        </div>
      </div>
    );
  }

  const { totalQuestions = 0, correct = 0, incorrect = 0, skipped = 0, marks = 0, accuracy = 0, averageTimeTaken = 0, questionDetails = [] } = result || {};
  const maxMarks = totalQuestions * 4;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Quiz Results</h1>
            <p className="mt-1 text-gray-500">Review your performance and learn from mistakes.</p>
          </div>
          <div className="flex gap-3">
            <Link to="/dashboard" className="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50">Dashboard</Link>
            <Link to="/subjects" className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700">Practice More</Link>
          </div>
        </div>

        {/* Score Card */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 sm:p-8 mb-8">
          <div className="text-center mb-6">
            <p className="text-5xl font-bold text-indigo-600">{marks}</p>
            <p className="text-gray-500 mt-1">out of {maxMarks} marks</p>
            <p className="text-sm text-gray-400 mt-0.5">+4 correct, -1 incorrect, 0 skipped</p>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-4">
            <div className="text-center p-4 bg-gray-50 rounded-xl">
              <p className="text-2xl font-bold text-gray-900">{totalQuestions}</p>
              <p className="text-xs text-gray-500 mt-0.5">Total</p>
            </div>
            <div className="text-center p-4 bg-green-50 rounded-xl">
              <p className="text-2xl font-bold text-green-600">{correct}</p>
              <p className="text-xs text-gray-500 mt-0.5">Correct</p>
            </div>
            <div className="text-center p-4 bg-red-50 rounded-xl">
              <p className="text-2xl font-bold text-red-600">{incorrect}</p>
              <p className="text-xs text-gray-500 mt-0.5">Incorrect</p>
            </div>
            <div className="text-center p-4 bg-amber-50 rounded-xl">
              <p className="text-2xl font-bold text-amber-600">{skipped}</p>
              <p className="text-xs text-gray-500 mt-0.5">Skipped</p>
            </div>
            <div className="text-center p-4 bg-indigo-50 rounded-xl col-span-2 sm:col-span-1">
              <p className="text-2xl font-bold text-indigo-600">{accuracy}%</p>
              <p className="text-xs text-gray-500 mt-0.5">Accuracy</p>
            </div>
          </div>
          {averageTimeTaken > 0 && (
            <p className="text-center text-sm text-gray-500 mt-4">Average time per question: <span className="font-medium text-gray-700">{averageTimeTaken}s</span></p>
          )}
        </div>

        {/* Question Review */}
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Question Review</h2>
        <div className="space-y-4">
          {questionDetails.map((q, idx) => {
            const isCorrectAnswer = q.isCorrect;
            const isSkipped = !q.selectedAnswer;
            let borderClass = 'border-red-200 bg-red-50/30';
            if (isSkipped) borderClass = 'border-amber-200 bg-amber-50/30';
            else if (isCorrectAnswer) borderClass = 'border-green-200 bg-green-50/30';

            const opts = [
              { label: 'A', text: q.optionA },
              { label: 'B', text: q.optionB },
              { label: 'C', text: q.optionC },
              { label: 'D', text: q.optionD },
            ];

            return (
              <div key={q.id || idx} className={`rounded-xl border-2 p-5 sm:p-6 ${borderClass}`}>
                <div className="flex items-start justify-between gap-4 mb-4">
                  <div className="flex items-start gap-3">
                    <span className={`shrink-0 w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white ${isSkipped ? 'bg-amber-500' : isCorrectAnswer ? 'bg-green-500' : 'bg-red-500'}`}>{idx + 1}</span>
                    <p className="text-gray-900 leading-relaxed">{q.questionText}</p>
                  </div>
                  <button onClick={() => toggleBookmark(q.id)} className={`shrink-0 p-1.5 rounded-lg cursor-pointer ${bookmarked[q.id] ? 'text-amber-500' : 'text-gray-400 hover:text-amber-400'}`}>
                    <svg className="w-5 h-5" fill={bookmarked[q.id] ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" /></svg>
                  </button>
                </div>

                <div className="space-y-2 ml-10">
                  {opts.map((opt) => {
                    const isThisCorrect = opt.label === q.correctAnswer;
                    const isThisSelected = opt.label === q.selectedAnswer;
                    let optClass = 'bg-white border-gray-200 text-gray-700';
                    if (isThisCorrect) optClass = 'bg-green-100 border-green-300 text-green-800';
                    if (isThisSelected && !isThisCorrect) optClass = 'bg-red-100 border-red-300 text-red-800';
                    return (
                      <div key={opt.label} className={`flex items-center gap-3 p-3 rounded-lg border ${optClass}`}>
                        <span className="text-xs font-semibold w-6 text-center">{opt.label}</span>
                        <span className="text-sm flex-1">{opt.text}</span>
                        {isThisCorrect && <svg className="w-4 h-4 text-green-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>}
                        {isThisSelected && !isThisCorrect && <svg className="w-4 h-4 text-red-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>}
                      </div>
                    );
                  })}
                </div>

                {q.explanation && (
                  <div className="mt-4 ml-10 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                    <p className="text-xs font-semibold text-blue-700 mb-1">Explanation</p>
                    <p className="text-sm text-blue-900 leading-relaxed">{q.explanation}</p>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
