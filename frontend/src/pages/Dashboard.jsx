import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import api from '../api';

export default function Dashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const res = await api.get('/analytics/dashboard');
        setData(res.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading dashboard...</p>
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

  const stats = [
    {
      label: 'Total Attempted',
      value: data?.overallStats?.totalAttempted ?? 0,
      color: 'bg-blue-50 text-blue-700',
      icon: (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
      ),
    },
    {
      label: 'Correct Answers',
      value: data?.overallStats?.correctAnswers ?? 0,
      color: 'bg-green-50 text-green-700',
      icon: (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
        </svg>
      ),
    },
    {
      label: 'Accuracy',
      value: `${data?.overallStats?.accuracy ?? 0}%`,
      color: 'bg-indigo-50 text-indigo-700',
      icon: (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
        </svg>
      ),
    },
    {
      label: 'Total Marks',
      value: data?.overallStats?.totalMarks ?? 0,
      color: 'bg-amber-50 text-amber-700',
      icon: (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
        </svg>
      ),
    },
  ];

  const quickActions = [
    { label: 'Practice', to: '/subjects', color: 'bg-indigo-600 hover:bg-indigo-700', icon: '>' },
    { label: 'Mock Test', to: '/mock-test', color: 'bg-purple-600 hover:bg-purple-700', icon: '>' },
    { label: 'Revision', to: '/bookmarks', color: 'bg-emerald-600 hover:bg-emerald-700', icon: '>' },
    { label: 'Search', to: '/search', color: 'bg-amber-600 hover:bg-amber-700', icon: '>' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome */}
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">
            Welcome back, {user?.name || 'Student'}
          </h1>
          <p className="mt-1 text-gray-500">Here is your preparation overview.</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          {stats.map((stat) => (
            <div
              key={stat.label}
              className="bg-white rounded-xl p-5 border border-gray-100 hover:shadow-md transition-shadow"
            >
              <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${stat.color} mb-3`}>
                {stat.icon}
              </div>
              <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              <p className="text-sm text-gray-500 mt-0.5">{stat.label}</p>
            </div>
          ))}
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          {/* Weak Topics */}
          <div className="lg:col-span-2 bg-white rounded-xl border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Weak Topics</h2>
            {data?.weakTopics?.length > 0 ? (
              <div className="space-y-3">
                {data.weakTopics.map((topic, idx) => (
                  <div key={idx} className="flex items-center justify-between p-3 bg-red-50 rounded-lg">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{topic.topic || topic.chapterName}</p>
                      <p className="text-xs text-gray-500">{topic.subject || topic.subjectName}</p>
                    </div>
                    <span className="text-sm font-semibold text-red-600">
                      {topic.accuracy != null ? `${topic.accuracy}%` : 'Needs Review'}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-400 text-sm">No weak topics identified yet. Keep practicing!</p>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Questions Due */}
            <div className="bg-white rounded-xl border border-gray-100 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-2">Due for Review</h2>
              <p className="text-4xl font-bold text-indigo-600">{data?.questionsForReview ?? 0}</p>
              <p className="text-sm text-gray-500 mt-1">Questions scheduled for revision</p>
              {(data?.questionsForReview ?? 0) > 0 && (
                <Link
                  to="/bookmarks"
                  className="mt-4 inline-block text-sm font-medium text-indigo-600 hover:text-indigo-700"
                >
                  Start Revision &rarr;
                </Link>
              )}
            </div>

            {/* Quick Actions */}
            <div className="bg-white rounded-xl border border-gray-100 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
              <div className="grid grid-cols-2 gap-3">
                {quickActions.map((action) => (
                  <Link
                    key={action.label}
                    to={action.to}
                    className={`${action.color} text-white text-sm font-medium rounded-lg py-3 px-4 text-center transition-colors`}
                  >
                    {action.label}
                  </Link>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
