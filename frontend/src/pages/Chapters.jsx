import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../api';

export default function Chapters() {
  const { subjectId } = useParams();
  const navigate = useNavigate();
  const [chapters, setChapters] = useState([]);
  const [subjectName, setSubjectName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchChapters = async () => {
      try {
        const res = await api.get(`/subjects/${subjectId}/chapters`);
        if (Array.isArray(res.data)) {
          setChapters(res.data);
        } else {
          setChapters(res.data.chapters || []);
          setSubjectName(res.data.subjectName || res.data.name || '');
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load chapters.');
      } finally {
        setLoading(false);
      }
    };
    fetchChapters();
  }, [subjectId]);

  const startPractice = (chapterId) => {
    const params = new URLSearchParams({
      chapterId,
      quizType: 'practice',
      questionCount: '20',
    });
    navigate(`/quiz?${params.toString()}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading chapters...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl p-8 border border-red-200 max-w-md text-center">
          <p className="text-red-600 font-medium">{error}</p>
          <Link
            to="/subjects"
            className="mt-4 inline-block px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700"
          >
            Back to Subjects
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-gray-500 mb-6">
          <Link to="/subjects" className="hover:text-indigo-600">Subjects</Link>
          <span>/</span>
          <span className="text-gray-900 font-medium">{subjectName || 'Chapters'}</span>
        </div>

        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">
            {subjectName || 'Chapters'}
          </h1>
          <p className="mt-1 text-gray-500">Select a chapter to start practicing.</p>
        </div>

        {chapters.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
            <p className="text-gray-400">No chapters available for this subject.</p>
          </div>
        ) : (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {chapters.map((chapter) => (
              <div
                key={chapter._id || chapter.id}
                className="bg-white rounded-xl border border-gray-100 p-6 hover:border-indigo-200 hover:shadow-md transition-all"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="w-10 h-10 bg-indigo-50 rounded-lg flex items-center justify-center text-indigo-600 shrink-0">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                </div>
                <h3 className="font-semibold text-gray-900">{chapter.name}</h3>
                <p className="text-sm text-gray-500 mt-1">
                  {chapter.questionCount ?? chapter.questions?.length ?? 0} Questions
                </p>
                <button
                  onClick={() => startPractice(chapter._id || chapter.id)}
                  className="mt-4 w-full py-2 px-4 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors cursor-pointer"
                >
                  Start Practice
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
