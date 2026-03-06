import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import api from '../api';

export default function Leaderboard() {
  const { user } = useAuth();
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchLeaderboard = async () => {
      try {
        const res = await api.get('/leaderboard/weekly');
        setEntries(res.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load leaderboard.');
      } finally {
        setLoading(false);
      }
    };
    fetchLeaderboard();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <p className="text-gray-500 text-sm">Loading leaderboard...</p>
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

  const getRankBadge = (rank) => {
    if (rank === 1) return 'bg-amber-400 text-white';
    if (rank === 2) return 'bg-gray-300 text-gray-800';
    if (rank === 3) return 'bg-amber-700 text-white';
    return 'bg-gray-100 text-gray-600';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Weekly Leaderboard</h1>
          <p className="mt-1 text-gray-500">See how you rank among other NEET PG aspirants this week.</p>
        </div>

        {entries.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
            <p className="text-gray-400">No leaderboard data available for this week yet.</p>
          </div>
        ) : (
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
            {/* Top 3 Podium */}
            {entries.length >= 3 && (
              <div className="bg-gradient-to-r from-indigo-600 to-indigo-700 p-6 sm:p-8">
                <div className="flex items-end justify-center gap-4 sm:gap-8">
                  {/* 2nd Place */}
                  <div className="text-center">
                    <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-white/20 flex items-center justify-center mx-auto mb-2">
                      <span className="text-white font-bold text-lg">
                        {(entries[1]?.name || entries[1]?.userName)?.[0] || '2'}
                      </span>
                    </div>
                    <p className="text-white text-sm font-medium truncate max-w-[90px]">
                      {entries[1]?.name || entries[1]?.userName}
                    </p>
                    <p className="text-indigo-200 text-xs">{entries[1]?.totalMarks ?? 0} marks</p>
                    <div className="mt-2 w-14 sm:w-16 h-16 bg-gray-300/30 rounded-t-lg mx-auto flex items-center justify-center">
                      <span className="text-white font-bold text-xl">2</span>
                    </div>
                  </div>

                  {/* 1st Place */}
                  <div className="text-center">
                    <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-full bg-amber-400/30 flex items-center justify-center mx-auto mb-2 ring-2 ring-amber-400">
                      <span className="text-white font-bold text-xl">
                        {(entries[0]?.name || entries[0]?.userName)?.[0] || '1'}
                      </span>
                    </div>
                    <p className="text-white text-sm font-semibold truncate max-w-[100px]">
                      {entries[0]?.name || entries[0]?.userName}
                    </p>
                    <p className="text-indigo-200 text-xs">{entries[0]?.totalMarks ?? 0} marks</p>
                    <div className="mt-2 w-16 sm:w-20 h-24 bg-amber-400/30 rounded-t-lg mx-auto flex items-center justify-center">
                      <span className="text-white font-bold text-2xl">1</span>
                    </div>
                  </div>

                  {/* 3rd Place */}
                  <div className="text-center">
                    <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-white/20 flex items-center justify-center mx-auto mb-2">
                      <span className="text-white font-bold text-lg">
                        {(entries[2]?.name || entries[2]?.userName)?.[0] || '3'}
                      </span>
                    </div>
                    <p className="text-white text-sm font-medium truncate max-w-[90px]">
                      {entries[2]?.name || entries[2]?.userName}
                    </p>
                    <p className="text-indigo-200 text-xs">{entries[2]?.totalMarks ?? 0} marks</p>
                    <div className="mt-2 w-14 sm:w-16 h-12 bg-amber-700/30 rounded-t-lg mx-auto flex items-center justify-center">
                      <span className="text-white font-bold text-xl">3</span>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Full Table */}
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200 bg-gray-50">
                    <th className="text-center py-3 px-4 font-semibold text-gray-700 w-16">Rank</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Name</th>
                    <th className="text-center py-3 px-4 font-semibold text-gray-700">Total Marks</th>
                    <th className="text-center py-3 px-4 font-semibold text-gray-700">Correct</th>
                  </tr>
                </thead>
                <tbody>
                  {entries.map((entry, idx) => {
                    const rank = entry.rank ?? idx + 1;
                    const userId = entry.userId || entry._id || entry.id;
                    const isCurrentUser = userId === (user?._id || user?.id || user?.userId);

                    return (
                      <tr
                        key={userId || idx}
                        className={`border-b border-gray-50 ${
                          isCurrentUser ? 'bg-indigo-50' : 'hover:bg-gray-50'
                        }`}
                      >
                        <td className="py-3 px-4 text-center">
                          <span className={`inline-flex items-center justify-center w-8 h-8 rounded-full text-xs font-bold ${getRankBadge(rank)}`}>
                            {rank}
                          </span>
                        </td>
                        <td className="py-3 px-4">
                          <span className="font-medium text-gray-900">
                            {entry.name || entry.userName}
                          </span>
                          {isCurrentUser && (
                            <span className="ml-2 text-xs text-indigo-600 font-medium">(You)</span>
                          )}
                        </td>
                        <td className="py-3 px-4 text-center font-semibold text-gray-900">
                          {entry.totalMarks ?? 0}
                        </td>
                        <td className="py-3 px-4 text-center text-gray-600">
                          {entry.correct ?? entry.totalCorrect ?? 0}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
