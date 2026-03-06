import { useState, useEffect } from 'react';
import api from '../api';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
} from 'recharts';

export default function Analytics() {
  const [overview, setOverview] = useState(null);
  const [subjectStats, setSubjectStats] = useState([]);
  const [dailyProgress, setDailyProgress] = useState([]);
  const [topicStrength, setTopicStrength] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const [overviewRes, subjectRes, dailyRes, topicRes] = await Promise.allSettled([
          api.get('/analytics/overall'),
          api.get('/analytics/subjects'),
          api.get('/analytics/daily?days=30'),
          api.get('/analytics/chapters'),
        ]);

        if (overviewRes.status === 'fulfilled') setOverview(overviewRes.value.data);
        if (subjectRes.status === 'fulfilled') {
          const data = (subjectRes.value.data || []).map(s => ({
            subject: s.subjectName,
            accuracy: s.accuracy,
            attempted: s.totalAttempted,
          }));
          setSubjectStats(data);
        }
        if (dailyRes.status === 'fulfilled') {
          const data = (dailyRes.value.data || []).map(d => ({
            date: d.date,
            questionsAttempted: d.totalAttempted,
            accuracy: d.accuracy,
          }));
          setDailyProgress(data);
        }
        if (topicRes.status === 'fulfilled') setTopicStrength(topicRes.value.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load analytics.');
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading analytics...</p>
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

  const statCards = [
    { label: 'Total Attempted', value: overview?.totalAttempted ?? 0, color: 'text-blue-600 bg-blue-50' },
    { label: 'Correct', value: overview?.correctAnswers ?? 0, color: 'text-green-600 bg-green-50' },
    { label: 'Accuracy', value: `${overview?.accuracy ?? 0}%`, color: 'text-indigo-600 bg-indigo-50' },
    { label: 'Total Marks', value: overview?.totalMarks ?? 0, color: 'text-amber-600 bg-amber-50' },
  ];

  const getStrengthBadge = (strength) => {
    const s = (strength || '').toLowerCase();
    if (s === 'strong') return 'bg-green-100 text-green-700';
    if (s === 'average') return 'bg-amber-100 text-amber-700';
    return 'bg-red-100 text-red-700';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Analytics</h1>
          <p className="mt-1 text-gray-500">Track your preparation progress and identify areas to improve.</p>
        </div>

        {/* Overview Stats */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          {statCards.map((stat) => (
            <div key={stat.label} className="bg-white rounded-xl border border-gray-100 p-5">
              <div className={`inline-flex items-center justify-center w-10 h-10 rounded-lg mb-3 ${stat.color}`}>
                <span className="text-lg font-bold">#</span>
              </div>
              <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              <p className="text-sm text-gray-500 mt-0.5">{stat.label}</p>
            </div>
          ))}
        </div>

        <div className="grid lg:grid-cols-2 gap-6 mb-8">
          {/* Subject-wise Accuracy Bar Chart */}
          <div className="bg-white rounded-xl border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Subject-wise Accuracy</h2>
            {subjectStats.length > 0 ? (
              <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={subjectStats} margin={{ top: 5, right: 20, left: 0, bottom: 60 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis
                      dataKey="subject"
                      tick={{ fontSize: 11 }}
                      angle={-45}
                      textAnchor="end"
                      height={80}
                    />
                    <YAxis tick={{ fontSize: 12 }} domain={[0, 100]} />
                    <Tooltip
                      formatter={(value) => [`${value}%`, 'Accuracy']}
                      contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb' }}
                    />
                    <Bar dataKey="accuracy" fill="#4f46e5" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <p className="text-gray-400 text-sm h-80 flex items-center justify-center">
                No subject data available yet.
              </p>
            )}
          </div>

          {/* Daily Progress Line Chart */}
          <div className="bg-white rounded-xl border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Daily Progress (Last 30 Days)</h2>
            {dailyProgress.length > 0 ? (
              <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={dailyProgress} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                    <YAxis tick={{ fontSize: 12 }} />
                    <Tooltip
                      contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb' }}
                    />
                    <Line
                      type="monotone"
                      dataKey="questionsAttempted"
                      name="Questions"
                      stroke="#4f46e5"
                      strokeWidth={2}
                      dot={{ fill: '#4f46e5', r: 3 }}
                      activeDot={{ r: 5 }}
                    />
                    <Line
                      type="monotone"
                      dataKey="accuracy"
                      name="Accuracy %"
                      stroke="#10b981"
                      strokeWidth={2}
                      dot={{ fill: '#10b981', r: 3 }}
                      activeDot={{ r: 5 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <p className="text-gray-400 text-sm h-80 flex items-center justify-center">
                No daily progress data available yet.
              </p>
            )}
          </div>
        </div>

        {/* Topic Strength Table */}
        <div className="bg-white rounded-xl border border-gray-100 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Topic Strength Analysis</h2>
          {topicStrength.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Topic</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Subject</th>
                    <th className="text-center py-3 px-4 font-semibold text-gray-700">Attempted</th>
                    <th className="text-center py-3 px-4 font-semibold text-gray-700">Accuracy</th>
                    <th className="text-center py-3 px-4 font-semibold text-gray-700">Strength</th>
                  </tr>
                </thead>
                <tbody>
                  {topicStrength.map((topic, idx) => (
                    <tr key={idx} className="border-b border-gray-50 hover:bg-gray-50">
                      <td className="py-3 px-4 font-medium text-gray-900">
                        {topic.topic || topic.chapterName}
                      </td>
                      <td className="py-3 px-4 text-gray-600">
                        {topic.subject || topic.subjectName}
                      </td>
                      <td className="py-3 px-4 text-center text-gray-600">
                        {topic.attempted ?? topic.totalAttempted ?? 0}
                      </td>
                      <td className="py-3 px-4 text-center text-gray-600">
                        {topic.accuracy ?? 0}%
                      </td>
                      <td className="py-3 px-4 text-center">
                        <span className={`inline-block px-2.5 py-1 rounded-full text-xs font-semibold ${getStrengthBadge(topic.strength)}`}>
                          {topic.strength || 'Weak'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-400 text-sm py-12 text-center">
              No topic strength data available yet. Start practicing to see insights.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
