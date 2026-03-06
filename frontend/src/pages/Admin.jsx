import { useState, useEffect } from 'react';
import api from '../api';

const TABS = [
  { key: 'add', label: 'Add Question' },
  { key: 'csv', label: 'Upload CSV' },
  { key: 'subjects', label: 'Manage Subjects' },
];

const emptyQuestion = {
  questionText: '',
  options: ['', '', '', ''],
  correctOption: 0,
  explanation: '',
  subjectId: '',
  chapterId: '',
  difficulty: 'Medium',
  previousYearTag: '',
};

export default function Admin() {
  const [activeTab, setActiveTab] = useState('add');
  const [subjects, setSubjects] = useState([]);
  const [chapters, setChapters] = useState([]);

  // Add Question state
  const [form, setForm] = useState({ ...emptyQuestion });
  const [addLoading, setAddLoading] = useState(false);
  const [addMsg, setAddMsg] = useState({ type: '', text: '' });

  // CSV Upload state
  const [csvFile, setCsvFile] = useState(null);
  const [csvSubjectId, setCsvSubjectId] = useState('');
  const [csvChapterId, setCsvChapterId] = useState('');
  const [csvChapters, setCsvChapters] = useState([]);
  const [csvLoading, setCsvLoading] = useState(false);
  const [csvMsg, setCsvMsg] = useState({ type: '', text: '' });

  // Subject/Chapter creation state
  const [newSubjectName, setNewSubjectName] = useState('');
  const [subjectLoading, setSubjectLoading] = useState(false);
  const [subjectMsg, setSubjectMsg] = useState({ type: '', text: '' });
  const [newChapterName, setNewChapterName] = useState('');
  const [chapterSubjectId, setChapterSubjectId] = useState('');
  const [chapterLoading, setChapterLoading] = useState(false);
  const [chapterMsg, setChapterMsg] = useState({ type: '', text: '' });

  // Load subjects
  useEffect(() => {
    const fetchSubjects = async () => {
      try {
        const res = await api.get('/subjects');
        setSubjects(res.data || []);
      } catch { /* silent */ }
    };
    fetchSubjects();
  }, []);

  // Load chapters when subject changes (add question)
  useEffect(() => {
    if (!form.subjectId) { setChapters([]); return; }
    const fetchChapters = async () => {
      try {
        const res = await api.get(`/subjects/${form.subjectId}/chapters`);
        setChapters(Array.isArray(res.data) ? res.data : res.data.chapters || []);
      } catch { setChapters([]); }
    };
    fetchChapters();
  }, [form.subjectId]);

  // Load chapters when subject changes (csv)
  useEffect(() => {
    if (!csvSubjectId) { setCsvChapters([]); return; }
    const fetchChapters = async () => {
      try {
        const res = await api.get(`/subjects/${csvSubjectId}/chapters`);
        setCsvChapters(Array.isArray(res.data) ? res.data : res.data.chapters || []);
      } catch { setCsvChapters([]); }
    };
    fetchChapters();
  }, [csvSubjectId]);

  // --- Handlers ---

  const handleFormChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleOptionChange = (index, value) => {
    setForm((prev) => {
      const options = [...prev.options];
      options[index] = value;
      return { ...prev, options };
    });
  };

  const handleAddQuestion = async (e) => {
    e.preventDefault();
    setAddLoading(true);
    setAddMsg({ type: '', text: '' });
    try {
      await api.post('/admin/questions', form);
      setAddMsg({ type: 'success', text: 'Question added successfully!' });
      setForm({ ...emptyQuestion });
    } catch (err) {
      setAddMsg({ type: 'error', text: err.response?.data?.message || 'Failed to add question.' });
    } finally {
      setAddLoading(false);
    }
  };

  const handleCsvUpload = async (e) => {
    e.preventDefault();
    if (!csvFile) return;
    setCsvLoading(true);
    setCsvMsg({ type: '', text: '' });
    try {
      const formData = new FormData();
      formData.append('file', csvFile);
      if (csvSubjectId) formData.append('subjectId', csvSubjectId);
      if (csvChapterId) formData.append('chapterId', csvChapterId);

      await api.post('/admin/questions/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setCsvMsg({ type: 'success', text: 'CSV uploaded and questions imported successfully!' });
      setCsvFile(null);
    } catch (err) {
      setCsvMsg({ type: 'error', text: err.response?.data?.message || 'Failed to upload CSV.' });
    } finally {
      setCsvLoading(false);
    }
  };

  const handleCreateSubject = async (e) => {
    e.preventDefault();
    if (!newSubjectName.trim()) return;
    setSubjectLoading(true);
    setSubjectMsg({ type: '', text: '' });
    try {
      const res = await api.post('/admin/subjects', { name: newSubjectName.trim() });
      setSubjects((prev) => [...prev, res.data]);
      setSubjectMsg({ type: 'success', text: 'Subject created successfully!' });
      setNewSubjectName('');
    } catch (err) {
      setSubjectMsg({ type: 'error', text: err.response?.data?.message || 'Failed to create subject.' });
    } finally {
      setSubjectLoading(false);
    }
  };

  const handleCreateChapter = async (e) => {
    e.preventDefault();
    if (!newChapterName.trim() || !chapterSubjectId) return;
    setChapterLoading(true);
    setChapterMsg({ type: '', text: '' });
    try {
      await api.post('/admin/chapters', {
        name: newChapterName.trim(),
        subjectId: chapterSubjectId,
      });
      setChapterMsg({ type: 'success', text: 'Chapter created successfully!' });
      setNewChapterName('');
    } catch (err) {
      setChapterMsg({ type: 'error', text: err.response?.data?.message || 'Failed to create chapter.' });
    } finally {
      setChapterLoading(false);
    }
  };

  const optionLabels = ['A', 'B', 'C', 'D'];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Admin Panel</h1>
          <p className="mt-1 text-gray-500">Manage questions, subjects, and content.</p>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-gray-200 mb-8 overflow-x-auto">
          {TABS.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`px-5 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition-colors cursor-pointer ${
                activeTab === tab.key
                  ? 'border-indigo-600 text-indigo-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Add Question Tab */}
        {activeTab === 'add' && (
          <div className="bg-white rounded-xl border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Add New Question</h2>

            {addMsg.text && (
              <div className={`mb-4 p-3 rounded-lg text-sm ${
                addMsg.type === 'success' ? 'bg-green-50 border border-green-200 text-green-700' : 'bg-red-50 border border-red-200 text-red-700'
              }`}>
                {addMsg.text}
              </div>
            )}

            <form onSubmit={handleAddQuestion} className="space-y-5">
              {/* Subject & Chapter */}
              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Subject</label>
                  <select
                    value={form.subjectId}
                    onChange={(e) => handleFormChange('subjectId', e.target.value)}
                    required
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="">Select Subject</option>
                    {subjects.map((s) => (
                      <option key={s._id || s.id} value={s._id || s.id}>{s.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Chapter</label>
                  <select
                    value={form.chapterId}
                    onChange={(e) => handleFormChange('chapterId', e.target.value)}
                    required
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="">Select Chapter</option>
                    {chapters.map((c) => (
                      <option key={c._id || c.id} value={c._id || c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Question Text */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Question Text</label>
                <textarea
                  value={form.questionText}
                  onChange={(e) => handleFormChange('questionText', e.target.value)}
                  required
                  rows={4}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-y placeholder-gray-400"
                  placeholder="Enter the question text..."
                />
              </div>

              {/* Options */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Options</label>
                <div className="space-y-3">
                  {form.options.map((opt, idx) => (
                    <div key={idx} className="flex items-center gap-3">
                      <span className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center text-sm font-semibold text-gray-600 shrink-0">
                        {optionLabels[idx]}
                      </span>
                      <input
                        type="text"
                        value={opt}
                        onChange={(e) => handleOptionChange(idx, e.target.value)}
                        required
                        className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-400"
                        placeholder={`Option ${optionLabels[idx]}`}
                      />
                      <label className="flex items-center gap-1.5 cursor-pointer shrink-0">
                        <input
                          type="radio"
                          name="correctOption"
                          checked={form.correctOption === idx}
                          onChange={() => handleFormChange('correctOption', idx)}
                          className="w-4 h-4 text-indigo-600"
                        />
                        <span className="text-xs text-gray-500">Correct</span>
                      </label>
                    </div>
                  ))}
                </div>
              </div>

              {/* Explanation */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Explanation</label>
                <textarea
                  value={form.explanation}
                  onChange={(e) => handleFormChange('explanation', e.target.value)}
                  rows={3}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-y placeholder-gray-400"
                  placeholder="Enter the explanation (optional)..."
                />
              </div>

              {/* Difficulty & Tag */}
              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Difficulty</label>
                  <select
                    value={form.difficulty}
                    onChange={(e) => handleFormChange('difficulty', e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="Easy">Easy</option>
                    <option value="Medium">Medium</option>
                    <option value="Hard">Hard</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Previous Year Tag</label>
                  <input
                    type="text"
                    value={form.previousYearTag}
                    onChange={(e) => handleFormChange('previousYearTag', e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-400"
                    placeholder="e.g., NEET PG 2024"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={addLoading}
                className="w-full py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer transition-colors"
              >
                {addLoading ? 'Adding Question...' : 'Add Question'}
              </button>
            </form>
          </div>
        )}

        {/* CSV Upload Tab */}
        {activeTab === 'csv' && (
          <div className="bg-white rounded-xl border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-6">Upload Questions via CSV</h2>

            {csvMsg.text && (
              <div className={`mb-4 p-3 rounded-lg text-sm ${
                csvMsg.type === 'success' ? 'bg-green-50 border border-green-200 text-green-700' : 'bg-red-50 border border-red-200 text-red-700'
              }`}>
                {csvMsg.text}
              </div>
            )}

            <form onSubmit={handleCsvUpload} className="space-y-5">
              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Subject (optional)</label>
                  <select
                    value={csvSubjectId}
                    onChange={(e) => setCsvSubjectId(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="">Select Subject</option>
                    {subjects.map((s) => (
                      <option key={s._id || s.id} value={s._id || s.id}>{s.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1.5">Chapter (optional)</label>
                  <select
                    value={csvChapterId}
                    onChange={(e) => setCsvChapterId(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="">Select Chapter</option>
                    {csvChapters.map((c) => (
                      <option key={c._id || c.id} value={c._id || c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">CSV File</label>
                <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center hover:border-indigo-400 transition-colors">
                  <input
                    type="file"
                    accept=".csv"
                    onChange={(e) => setCsvFile(e.target.files[0])}
                    className="hidden"
                    id="csv-upload"
                  />
                  <label htmlFor="csv-upload" className="cursor-pointer">
                    <svg className="w-10 h-10 text-gray-400 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                    </svg>
                    {csvFile ? (
                      <p className="text-sm text-indigo-600 font-medium">{csvFile.name}</p>
                    ) : (
                      <>
                        <p className="text-sm text-gray-600 font-medium">Click to select a CSV file</p>
                        <p className="text-xs text-gray-400 mt-1">
                          Format: questionText, optionA, optionB, optionC, optionD, correctOption, explanation, difficulty
                        </p>
                      </>
                    )}
                  </label>
                </div>
              </div>

              <button
                type="submit"
                disabled={csvLoading || !csvFile}
                className="w-full py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer transition-colors"
              >
                {csvLoading ? 'Uploading...' : 'Upload CSV'}
              </button>
            </form>
          </div>
        )}

        {/* Manage Subjects Tab */}
        {activeTab === 'subjects' && (
          <div className="space-y-6">
            {/* Create Subject */}
            <div className="bg-white rounded-xl border border-gray-100 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Create Subject</h2>

              {subjectMsg.text && (
                <div className={`mb-4 p-3 rounded-lg text-sm ${
                  subjectMsg.type === 'success' ? 'bg-green-50 border border-green-200 text-green-700' : 'bg-red-50 border border-red-200 text-red-700'
                }`}>
                  {subjectMsg.text}
                </div>
              )}

              <form onSubmit={handleCreateSubject} className="flex gap-3">
                <input
                  type="text"
                  value={newSubjectName}
                  onChange={(e) => setNewSubjectName(e.target.value)}
                  placeholder="Subject name (e.g., Anatomy)"
                  required
                  className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-400"
                />
                <button
                  type="submit"
                  disabled={subjectLoading}
                  className="px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer transition-colors"
                >
                  {subjectLoading ? 'Creating...' : 'Create'}
                </button>
              </form>
            </div>

            {/* Create Chapter */}
            <div className="bg-white rounded-xl border border-gray-100 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Create Chapter</h2>

              {chapterMsg.text && (
                <div className={`mb-4 p-3 rounded-lg text-sm ${
                  chapterMsg.type === 'success' ? 'bg-green-50 border border-green-200 text-green-700' : 'bg-red-50 border border-red-200 text-red-700'
                }`}>
                  {chapterMsg.text}
                </div>
              )}

              <form onSubmit={handleCreateChapter} className="space-y-4">
                <div className="grid sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Subject</label>
                    <select
                      value={chapterSubjectId}
                      onChange={(e) => setChapterSubjectId(e.target.value)}
                      required
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
                    >
                      <option value="">Select Subject</option>
                      {subjects.map((s) => (
                        <option key={s._id || s.id} value={s._id || s.id}>{s.name}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Chapter Name</label>
                    <input
                      type="text"
                      value={newChapterName}
                      onChange={(e) => setNewChapterName(e.target.value)}
                      placeholder="e.g., Upper Limb"
                      required
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-400"
                    />
                  </div>
                </div>
                <button
                  type="submit"
                  disabled={chapterLoading}
                  className="px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer transition-colors"
                >
                  {chapterLoading ? 'Creating...' : 'Create Chapter'}
                </button>
              </form>
            </div>

            {/* Existing Subjects List */}
            <div className="bg-white rounded-xl border border-gray-100 p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Existing Subjects</h2>
              {subjects.length === 0 ? (
                <p className="text-gray-400 text-sm">No subjects created yet.</p>
              ) : (
                <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">
                  {subjects.map((s) => (
                    <div key={s._id || s.id} className="p-3 bg-gray-50 rounded-lg">
                      <p className="font-medium text-gray-900 text-sm">{s.name}</p>
                      <p className="text-xs text-gray-500 mt-0.5">
                        {s.chapterCount ?? s.chapters?.length ?? 0} chapters
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
