import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';

const features = [
  {
    title: '50,000+ Questions',
    description: 'Comprehensive question bank covering all 19 subjects of NEET PG with detailed explanations.',
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
      </svg>
    ),
  },
  {
    title: 'Smart Revision',
    description: 'Spaced repetition algorithm ensures you review questions at the optimal time for long-term retention.',
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
      </svg>
    ),
  },
  {
    title: 'Mock Tests',
    description: 'Full-length mock tests simulating the real NEET PG exam with 200 questions and 3.5-hour timer.',
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    title: 'Performance Analytics',
    description: 'Detailed analytics with subject-wise accuracy, daily progress tracking, and weak topic identification.',
    icon: (
      <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
    ),
  },
];

export default function Home() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <header className="bg-white border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">NP</span>
            </div>
            <span className="text-xl font-bold text-gray-900">
              NEET PG <span className="text-indigo-600">Pro</span>
            </span>
          </div>
          <div className="flex items-center gap-3">
            {user ? (
              <Link
                to="/dashboard"
                className="px-5 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors"
              >
                Go to Dashboard
              </Link>
            ) : (
              <>
                <Link
                  to="/login"
                  className="px-5 py-2.5 text-sm font-medium text-gray-700 hover:text-indigo-600 transition-colors"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="px-5 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors"
                >
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-50 via-white to-purple-50" />
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 sm:py-32">
          <div className="text-center max-w-3xl mx-auto">
            <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-indigo-100 text-indigo-700 rounded-full text-sm font-medium mb-6">
              <span className="w-2 h-2 bg-indigo-600 rounded-full" />
              Trusted by 10,000+ Medical Students
            </div>
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-gray-900 tracking-tight leading-tight">
              Ace NEET PG with
              <span className="text-indigo-600"> Smart Practice</span>
            </h1>
            <p className="mt-6 text-lg sm:text-xl text-gray-600 leading-relaxed">
              Master all 19 subjects with our AI-powered question bank, adaptive revision system,
              and comprehensive mock tests. Study smarter, not harder.
            </p>
            <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
              <Link
                to="/register"
                className="w-full sm:w-auto px-8 py-3.5 bg-indigo-600 text-white font-semibold rounded-xl hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200 text-center"
              >
                Start Practicing Free
              </Link>
              <Link
                to="/login"
                className="w-full sm:w-auto px-8 py-3.5 bg-white text-gray-700 font-semibold rounded-xl border border-gray-200 hover:border-indigo-300 hover:text-indigo-600 transition-colors text-center"
              >
                I have an account
              </Link>
            </div>
          </div>

          {/* Stats */}
          <div className="mt-20 grid grid-cols-2 md:grid-cols-4 gap-6 max-w-4xl mx-auto">
            {[
              { value: '50,000+', label: 'Questions' },
              { value: '19', label: 'Subjects' },
              { value: '10,000+', label: 'Students' },
              { value: '95%', label: 'Success Rate' },
            ].map((stat) => (
              <div key={stat.label} className="text-center">
                <p className="text-3xl sm:text-4xl font-bold text-indigo-600">{stat.value}</p>
                <p className="mt-1 text-sm text-gray-500 font-medium">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-20 sm:py-24 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-gray-900">
              Everything you need to crack NEET PG
            </h2>
            <p className="mt-4 text-lg text-gray-600">
              A complete preparation platform designed specifically for NEET PG aspirants.
            </p>
          </div>
          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature) => (
              <div
                key={feature.title}
                className="bg-white rounded-2xl p-6 border border-gray-100 hover:border-indigo-200 hover:shadow-lg transition-all group"
              >
                <div className="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                  {feature.icon}
                </div>
                <h3 className="mt-4 text-lg font-semibold text-gray-900">{feature.title}</h3>
                <p className="mt-2 text-sm text-gray-500 leading-relaxed">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 sm:py-24">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="bg-gradient-to-r from-indigo-600 to-indigo-700 rounded-3xl p-10 sm:p-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-white">
              Ready to start your journey?
            </h2>
            <p className="mt-4 text-indigo-100 text-lg">
              Join thousands of medical students already preparing smarter with NEET PG Pro.
            </p>
            <div className="mt-8 flex flex-col sm:flex-row justify-center gap-4">
              <Link
                to="/register"
                className="px-8 py-3.5 bg-white text-indigo-600 font-semibold rounded-xl hover:bg-gray-50 transition-colors"
              >
                Get Started
              </Link>
              <Link
                to="/login"
                className="px-8 py-3.5 bg-indigo-500 text-white font-semibold rounded-xl hover:bg-indigo-400 transition-colors border border-indigo-400"
              >
                Login
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-200 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-sm text-gray-500">
          &copy; {new Date().getFullYear()} NEET PG Pro. All rights reserved.
        </div>
      </footer>
    </div>
  );
}
