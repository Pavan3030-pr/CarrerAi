import React, { useMemo, useState, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import {
  Activity,
  BrainCircuit,
  CheckCircle2,
  FileText,
  Gauge,
  GraduationCap,
  LogIn,
  LogOut,
  Map,
  MessageSquareText,
  Rocket,
  ShieldCheck,
  Sparkles,
  Target,
  User,
  X,
  BarChart3,
  Users,
  TrendingUp,
  Clock,
  BookOpen,
  Award,
} from 'lucide-react';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

function App() {
  // View state
  const [view, setView] = useState('dashboard');

  // Auth state
  const [token, setToken] = useState(() => localStorage.getItem('careercompass_token') || '');
  const [user, setUser] = useState(() => {
    const t = localStorage.getItem('careercompass_token');
    const n = localStorage.getItem('careercompass_name');
    const e = localStorage.getItem('careercompass_email');
    return t ? { name: n || '', email: e || '' } : null;
  });
  const [showAuth, setShowAuth] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [authName, setAuthName] = useState('');
  const [authEmail, setAuthEmail] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authError, setAuthError] = useState('');
  const [authLoading, setAuthLoading] = useState(false);

  // Admin state
  const [adminData, setAdminData] = useState(null);
  const [adminUsers, setAdminUsers] = useState([]);
  const [adminLoading, setAdminLoading] = useState(false);

  // Profile & app state
  const [profile, setProfile] = useState({
    name: '', degree: '', targetRole: '',
    experienceLevel: 'Student', skills: '',
    interests: '',
  });
  const [plan, setPlan] = useState(null);
  const [resumeText, setResumeText] = useState('');
  const [resume, setResume] = useState(null);
  const [interviewAnswer, setInterviewAnswer] = useState('');
  const [feedback, setFeedback] = useState(null);
  const [loading, setLoading] = useState('');
  const [apiError, setApiError] = useState('');

  // Update profile name when user logs in
  useEffect(() => {
    if (user) {
      setProfile(prev => ({ ...prev, name: user.name }));
    }
  }, [user]);

  const modules = useMemo(() => [
    { icon: BrainCircuit, label: 'AI Career Profiling', value: plan ? `${plan.careerMatches[0]?.fit || 0}% role fit` : '—', sectionId: 'ai-career-profiling' },
    { icon: Target, label: 'Skill-Gap Matrix', value: plan ? `${plan.skillGaps.filter(g => g.readiness < 70).length} gaps found` : '—', sectionId: 'skill-gap-matrix' },
    { icon: Map, label: 'AI Roadmap', value: plan ? `${plan.roadmap.length} weeks planned` : '—', sectionId: 'ai-roadmap' },
    { icon: FileText, label: 'Resume Intelligence', value: resume ? `${resume.atsScore} ATS` : '—', sectionId: 'resume-intelligence' },
    { icon: MessageSquareText, label: 'Mock Interview AI', value: feedback ? `${feedback.score}/100` : '—', sectionId: 'mock-interview-ai' },
    { icon: Gauge, label: 'Live Readiness Score', value: plan ? `${plan.readinessScore}/100` : '—', sectionId: 'live-readiness-score' },
  ], [feedback, plan, resume]);

  function scrollToSection(sectionId) {
    const el = document.getElementById(sectionId);
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  // API helper with auth token - NO fallback, surfaces errors
  async function post(path, body) {
    setLoading(path);
    setApiError('');
    try {
      const headers = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = `Bearer ${token}`;
      const response = await fetch(`${API_BASE}${path}`, {
        method: 'POST', headers, body: JSON.stringify(body),
      });
      if (!response.ok) {
        const errBody = await response.json().catch(() => ({}));
        throw new Error(errBody.error || `Server error (${response.status}) — make sure the backend is running`);
      }
      return await response.json();
    } catch (error) {
      setApiError(error.message);
      throw error;
    } finally {
      setLoading('');
    }
  }

  // Auth handlers
  function openAuth(mode) {
    setAuthMode(mode);
    setAuthError('');
    setAuthName('');
    setAuthEmail('');
    setAuthPassword('');
    setShowAuth(true);
  }

  function logout() {
    setToken('');
    setUser(null);
    localStorage.removeItem('careercompass_token');
    localStorage.removeItem('careercompass_name');
    localStorage.removeItem('careercompass_email');
  }

  async function handleAuth(e) {
    e.preventDefault();
    setAuthLoading(true);
    setAuthError('');
    try {
      const endpoint = authMode === 'login' ? '/auth/login' : '/auth/register';
      const body = { name: authName, email: authEmail, password: authPassword };
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (!response.ok) {
        const err = await response.json();
        throw new Error(err.error || 'Authentication failed');
      }
      const data = await response.json();
      setToken(data.token);
      setUser({ name: data.name, email: data.email });
      localStorage.setItem('careercompass_token', data.token);
      localStorage.setItem('careercompass_name', data.name);
      localStorage.setItem('careercompass_email', data.email);
      setShowAuth(false);
    } catch (err) {
      setAuthError(err.message);
    } finally {
      setAuthLoading(false);
    }
  }

  // Demo mode constants
  const DEMO_PROFILE = {
    name: 'Pavan Sai',
    degree: 'B.Tech CSE',
    targetRole: 'Java Full Stack Developer',
    experienceLevel: 'Student',
    skills: 'Java, Spring Boot, React, SQL, Git',
    interests: 'AI, Web development, Placement preparation',
  };
  const DEMO_RESUME = 'Java full stack developer with Spring Boot, React, SQL and REST API projects. Built a placement preparation platform and improved dashboard tracking by 30%. Designed and deployed REST APIs for assessment, resume analysis, interview feedback, and personalized roadmaps. Owned the full-stack development lifecycle from requirements to deployment.';
  const DEMO_INTERVIEW = 'In my recent project, I built REST APIs with Spring Boot and connected them to a React frontend. The situation was challenging because we had to deliver a working prototype in 48 hours. My task was to own the backend architecture and API design. I took action by designing modular REST endpoints, implementing JWT authentication, and integrating the Gemini AI API for intelligent scoring. The result was a fully functional demo with authentication, career planning, resume analysis, and interview coaching that judges could interact with in real time.';

  // Demo mode
  const [demoMode, setDemoMode] = useState(false);

  function loadDemoData() {
    setDemoMode(true);
    setProfile(DEMO_PROFILE);
    setResumeText(DEMO_RESUME);
    setInterviewAnswer(DEMO_INTERVIEW);
    // Scroll to top after filling
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }


  // Feature handlers - NO fallback data, errors displayed to user
  async function generatePlan() {
    const payload = {
      ...profile,
      skills: profile.skills.split(',').map(s => s.trim()).filter(Boolean),
      interests: profile.interests.split(',').map(i => i.trim()).filter(Boolean),
    };
    const data = await post('/career-plan', payload);
    setPlan(data);
    setApiError('');
  }

  async function analyzeResume() {
    const data = await post('/resume/analyze', { targetRole: profile.targetRole, resumeText });
    setResume(data);
    setApiError('');
  }

  async function scoreInterview() {
    const data = await post('/interview/score', {
      targetRole: profile.targetRole, question: 'Tell me about your best full-stack project.', answer: interviewAnswer,
    });
    setFeedback(data);
    setApiError('');
  }

  // Admin data fetching
  async function fetchAdminData() {
    if (!token) return;
    setAdminLoading(true);
    try {
      const headers = { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` };
      const [overview, users] = await Promise.all([
        fetch(`${API_BASE}/admin/analytics/overview`, { headers }).then(r => r.ok ? r.json() : null),
        fetch(`${API_BASE}/admin/users`, { headers }).then(r => r.ok ? r.json() : null),
      ]);
      setAdminData(overview);
      setAdminUsers(users || []);
    } catch (e) {
      console.warn('Failed to fetch admin data', e);
    } finally {
      setAdminLoading(false);
    }
  }

  useEffect(() => {
    if (view === 'admin' && token) fetchAdminData();
  }, [view, token]);

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark"><Sparkles size={22} /></div>
          <div>
            <strong>CareerCompass AI</strong>
            <span>Personal AI career mentor</span>
          </div>
        </div>

        {/* User section */}
        {user ? (
          <div className="sidebar-user">
            <div className="user-avatar"><User size={18} /></div>
            <div>
              <strong>{user.name}</strong>
              <span>{user.email}</span>
            </div>
            <button className="user-logout" onClick={logout} title="Log out"><LogOut size={15} /></button>
          </div>
        ) : (
          <div className="sidebar-user">
            <button className="primary auth-btn" onClick={() => openAuth('login')}>
              <LogIn size={16} /> Sign In
            </button>
            <button className="secondary auth-btn" onClick={() => openAuth('register')}>
              Register
            </button>
          </div>
        )}

        <nav>
          <a href="#" onClick={e => { e.preventDefault(); setView('dashboard'); }} className={view === 'dashboard' && !window.location.hash ? 'active' : ''}>
            <BarChart3 size={18} />
            <span>Dashboard</span>
          </a>
          {modules.map(({ icon: Icon, label, sectionId }) => (
            <a href={`#${sectionId}`} key={label}
              onClick={e => { e.preventDefault(); navigateToSection(sectionId); }}>
              <Icon size={18} />
              <span>{label}</span>
            </a>
          ))}
          <a href="#" onClick={e => { e.preventDefault(); setView('admin'); }} className={view === 'admin' ? 'active' : ''}>
            <Users size={18} />
            <span>Admin Dashboard</span>
          </a>
        </nav>
        <div className="sidebar-proof">
          <ShieldCheck size={18} />
          <span>Powered by Gemini AI. Full-stack Spring Boot + React + JWT Auth.</span>
        </div>
      </aside>

      <section className="workspace">
        {view === 'admin' ? (
          <AdminDashboard
            data={adminData}
            users={adminUsers}
            loading={adminLoading}
            onRefresh={fetchAdminData}
          />
        ) : (
        <>
        <header className="topbar">
          <div>
            <p className="eyebrow">Hackathon MVP {user ? `· ${user.name}` : ''} {demoMode ? '· 🎯 Demo Mode' : ''}</p>
            <h1>One platform for assessment, gaps, roadmap, resume, interview, and readiness.</h1>
          </div>
          <div className="topbar-actions">
            <button
              className={`demo-toggle ${demoMode ? 'active' : ''}`}
              onClick={loadDemoData}
              title="Pre-fill all fields with demo data for a quick walkthrough"
            >
              <Sparkles size={16} />
              {demoMode ? '✨ Demo Loaded' : '🎯 Demo Mode'}
            </button>
            <button className="primary" onClick={generatePlan} disabled={loading === '/career-plan'}>
              <Rocket size={18} />
              {loading === '/career-plan' ? 'Generating...' : 'Generate AI Plan'}
            </button>
          </div>
        </header>

        {/* Auth modal */}
        {showAuth && (
          <div className="modal-overlay" onClick={() => setShowAuth(false)}>
            <div className="auth-modal" onClick={e => e.stopPropagation()}>
              <button className="modal-close" onClick={() => setShowAuth(false)}><X size={20} /></button>
              <h2>{authMode === 'login' ? 'Sign In' : 'Create Account'}</h2>
              <p className="auth-subtitle">
                {authMode === 'login' ? 'Welcome back! Sign in to save your progress.' : 'Join CareerCompass AI and start your career readiness journey.'}
              </p>
              <form onSubmit={handleAuth}>
                {authMode === 'register' && (
                  <label>Name
                    <input required value={authName} onChange={e => setAuthName(e.target.value)} placeholder="Your full name" />
                  </label>
                )}
                <label>Email
                  <input required type="email" value={authEmail} onChange={e => setAuthEmail(e.target.value)} placeholder="you@example.com" />
                </label>
                <label>Password
                  <input required type="password" value={authPassword} onChange={e => setAuthPassword(e.target.value)} placeholder="Min 6 characters" minLength={6} />
                </label>
                {authError && <p className="auth-error">{authError}</p>}
                <button className="primary auth-submit" disabled={authLoading}>
                  {authLoading ? 'Processing...' : authMode === 'login' ? 'Sign In' : 'Create Account'}
                </button>
              </form>
              <p className="auth-switch">
                {authMode === 'login' ? (
                  <>Don't have an account? <a href="#" onClick={e => { e.preventDefault(); openAuth('register'); }}>Register</a></>
                ) : (
                  <>Already have an account? <a href="#" onClick={e => { e.preventDefault(); openAuth('login'); }}>Sign In</a></>
                )}
              </p>
            </div>
          </div>
        )}

        {/* API Error banner */}
        {apiError && (
          <div className="api-error-banner">
            <span>⚠️ {apiError}</span>
            <button onClick={() => setApiError('')} className="error-dismiss"><X size={16} /></button>
          </div>
        )}

        {/* Metrics */}
        <section className="metrics-grid" aria-label="Career modules">
          {modules.map(({ icon: Icon, label, value, sectionId }) => (
            <article className="metric-card" key={label}>
              <Icon size={21} />
              <span>{label}</span>
              <strong>{value}</strong>
            </article>
          ))}
        </section>

        <section className="main-grid">
          <div className="panel profile-panel">
            <div className="panel-heading">
              <GraduationCap size={21} />
              <h2>Career Assessment</h2>
            </div>
            <div className="form-grid">
              <label>Name<input value={profile.name} onChange={e => setProfile({ ...profile, name: e.target.value })} placeholder="Your name" /></label>
              <label>Degree<input value={profile.degree} onChange={e => setProfile({ ...profile, degree: e.target.value })} placeholder="B.Tech CSE" /></label>
              <label>Target role<input value={profile.targetRole} onChange={e => setProfile({ ...profile, targetRole: e.target.value })} placeholder="Java Full Stack Developer" /></label>
              <label>Level<input value={profile.experienceLevel} onChange={e => setProfile({ ...profile, experienceLevel: e.target.value })} /></label>
              <label className="wide">Skills (comma-separated)<input value={profile.skills} onChange={e => setProfile({ ...profile, skills: e.target.value })} placeholder="Java, Spring Boot, React, SQL, Git" /></label>
              <label className="wide">Interests (comma-separated)<input value={profile.interests} onChange={e => setProfile({ ...profile, interests: e.target.value })} placeholder="AI, Web development" /></label>
            </div>
          </div>

          <div className="panel readiness-panel" id="live-readiness-score">
            {plan ? (
              <>
                <div className="score-ring" style={{ '--score': `${plan.readinessScore * 3.6}deg` }}>
                  <strong>{plan.readinessScore}</strong>
                  <span>Readiness</span>
                </div>
                <div>
                  <h2>Top Career Match</h2>
                  <h3>{plan.careerMatches[0]?.role}</h3>
                  <p>{plan.careerMatches[0]?.reason}</p>
                  <div className="match-list">
                    {plan.careerMatches.slice(0, 4).map(match => (
                      <div key={match.role}>
                        <span>{match.role}</span>
                        <meter min="0" max="100" value={match.fit} />
                        <strong>{match.fit}%</strong>
                      </div>
                    ))}
                  </div>
                </div>
              </>
            ) : (
              <div className="empty-state">
                <Gauge size={48} />
                <h3>No Career Plan Yet</h3>
                <p>Fill in your profile and click "Generate AI Plan" to see your readiness score and career matches.</p>
              </div>
            )}
          </div>
        </section>

        <section className="main-grid">
          <div className="panel" id="skill-gap-matrix">
            <div className="panel-heading">
              <Activity size={21} />
              <h2>Skill-Gap Matrix</h2>
            </div>
            {plan ? (
              <div className="gap-list">
                {plan.skillGaps.map(gap => (
                  <div className="gap-row" key={gap.skill}>
                    <span>{gap.skill}</span>
                    <div className="heat-track"><i style={{ width: `${gap.readiness}%` }} /></div>
                    <strong className={gap.priority.toLowerCase()}>{gap.priority}</strong>
                  </div>
                ))}
              </div>
            ) : (
              <p className="empty-state-text">Generate a career plan to see your skill gaps.</p>
            )}
          </div>

          <div className="panel" id="ai-roadmap">
            <div className="panel-heading">
              <Map size={21} />
              <h2>Week-by-Week Roadmap</h2>
            </div>
            {plan ? (
              <div className="roadmap">
                {plan.roadmap.map(week => (
                  <article key={week.week}>
                    <span>Week {week.week}</span>
                    <strong>{week.focus}</strong>
                    <p>{week.actions.join(' · ')}</p>
                    <small>{week.proofOfWork}</small>
                  </article>
                ))}
              </div>
            ) : (
              <p className="empty-state-text">Generate a career plan to see your personalized roadmap.</p>
            )}
          </div>
        </section>

        <section className="main-grid">
          <div className="panel" id="resume-intelligence">
            <div className="panel-heading">
              <FileText size={21} />
              <h2>Resume Intelligence</h2>
            </div>
            <textarea value={resumeText} onChange={e => setResumeText(e.target.value)}
              placeholder="Paste your resume text here, or upload a file..." />
            <div className="resume-actions">
              <label className="file-upload-btn secondary">
                <FileText size={17} />
                Upload Resume
                <input type="file" accept=".txt,.pdf,.doc,.docx" style={{ display: 'none' }}
                  onChange={e => {
                    const file = e.target.files?.[0];
                    if (file) {
                      const reader = new FileReader();
                      reader.onload = () => setResumeText(reader.result);
                      reader.readAsText(file);
                    }
                  }} />
              </label>
              <button className="secondary" onClick={analyzeResume} disabled={loading === '/resume/analyze' || !resumeText.trim()}>
                {loading === '/resume/analyze' ? 'Analyzing...' : 'Analyze Resume'}
              </button>
            </div>
            {resume && <ScoreSummary scores={[['ATS', resume.atsScore], ['Content', resume.contentScore], ['Impact', resume.impactScore]]} notes={resume.fixes} />}
          </div>

          <div className="panel" id="mock-interview-ai">
            <div className="panel-heading">
              <MessageSquareText size={21} />
              <h2>Mock Interview AI</h2>
            </div>
            <p className="question">Tell me about your best full-stack project.</p>
            <textarea value={interviewAnswer} onChange={e => setInterviewAnswer(e.target.value)}
              placeholder="Type your answer here..." />
            <button className="secondary" onClick={scoreInterview} disabled={loading === '/interview/score' || !interviewAnswer.trim()}>
              <MessageSquareText size={17} />
              {loading === '/interview/score' ? 'Scoring...' : 'Score Answer'}
            </button>
            {feedback && (
              <div className="feedback">
                <strong>{feedback.score}/100</strong>
                <p>{feedback.verdict}</p>
                {feedback.coachingTips.map(tip => <span key={tip}><CheckCircle2 size={15} />{tip}</span>)}
              </div>
            )}
          </div>
        </section>
        </>
        )}
      </section>
    </main>
  );
}

function AdminDashboard({ data, users, loading, onRefresh }) {
  return (
    <div className="admin-dashboard">
      <header className="topbar">
        <div>
          <p className="eyebrow">Admin Panel</p>
          <h1>Cohort Readiness Analytics</h1>
        </div>
        <button className="primary" onClick={onRefresh} disabled={loading}>
          <TrendingUp size={18} />
          {loading ? 'Loading...' : 'Refresh Data'}
        </button>
      </header>

      {data ? (
        <>
          <section className="admin-stats-grid">
            <div className="admin-stat-card">
              <Users size={24} />
              <strong>{data.totalUsers}</strong>
              <span>Total Users</span>
            </div>
            <div className="admin-stat-card">
              <BookOpen size={24} />
              <strong>{data.totalCareerPlans}</strong>
              <span>Career Plans</span>
            </div>
            <div className="admin-stat-card">
              <FileText size={24} />
              <strong>{data.totalResumeAnalyses}</strong>
              <span>Resume Analyses</span>
            </div>
            <div className="admin-stat-card">
              <MessageSquareText size={24} />
              <strong>{data.totalInterviewFeedbacks}</strong>
              <span>Interviews</span>
            </div>
            <div className="admin-stat-card">
              <Award size={24} />
              <strong>{data.averageReadinessScore}/100</strong>
              <span>Avg Readiness</span>
            </div>
            <div className="admin-stat-card">
              <TrendingUp size={24} />
              <strong>{data.averageInterviewScore}/100</strong>
              <span>Avg Interview</span>
            </div>
            <div className="admin-stat-card">
              <Clock size={24} />
              <strong>{data.recentSignups7Days}</strong>
              <span>Signups (7 days)</span>
            </div>
          </section>

          <div className="panel">
            <div className="panel-heading">
              <Users size={21} />
              <h2>Registered Users</h2>
            </div>
            {users.length > 0 ? (
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Joined</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.id}>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="admin-empty">No users registered yet. Sign in or register to see data here.</p>
            )}
          </div>
        </>
      ) : (
        <div className="panel">
          <p>Sign in and click "Refresh Data" to load analytics. Admin data requires authentication.</p>
        </div>
      )}
    </div>
  );
}

function ScoreSummary({ scores, notes }) {
  return (
    <div className="score-summary">
      {scores.map(([label, value]) => (
        <div key={label}><span>{label}</span><strong>{value}</strong></div>
      ))}
      <ul>{notes.map(note => <li key={note}>{note}</li>)}</ul>
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App />);
