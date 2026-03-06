import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';

export default function Subjects() {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchSubjects = async () => {
      try {
        const res = await api.get('/subjects');
        setSubjects(res.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load subjects.');
      } finally {
        setLoading(false);
      }
    };
    fetchSubjects();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading subjects...</p>
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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Subjects</h1>
          <p className="mt-1 text-gray-500">Select a subject to start practicing.</p>
        </div>

        {subjects.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
            <p className="text-gray-400">No subjects available yet.</p>
          </div>
        ) : (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {subjects.map((subject) => (
              <Link
                key={subject._id || subject.id}
                to={`/subjects/${subject._id || subject.id}/chapters`}
                className="bg-white rounded-xl border border-gray-100 p-6 hover:border-indigo-200 hover:shadow-md transition-all group"
              >
                <div className="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center mb-4 group-hover:bg-indigo-100 transition-colors">
                  <svg className="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                  </svg>
                </div>
                <h3 className="font-semibold text-gray-900 group-hover:text-indigo-600 transition-colors">
                  {subject.name}
                </h3>
                <p className="text-sm text-gray-500 mt-1">
                  {subject.chapterCount ?? subject.chapters?.length ?? 0} Chapters
                </p>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
