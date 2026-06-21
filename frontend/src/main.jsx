import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  Activity,
  BrainCircuit,
  CheckCircle2,
  FileText,
  Gauge,
  GraduationCap,
  Map,
  MessageSquareText,
  Rocket,
  ShieldCheck,
  Sparkles,
  Target,
} from 'lucide-react';
import './styles.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const defaultPlan = {
  readinessScore: 64,
  careerMatches: [
    { role: 'Java Full Stack Developer', fit: 87, reason: 'Best match for Spring, REST, React, and SQL skills.' },
    { role: 'Backend Engineer', fit: 78, reason: 'Strong path for APIs, services, and databases.' },
    { role: 'Cloud Engineer', fit: 61, reason: 'Future path after strengthening deployments.' },
  ],
  skillGaps: [
    { skill: 'Java', readiness: 82, priority: 'Ready', recommendation: 'Use this as a resume strength.' },
    { skill: 'Spring Boot', readiness: 72, priority: 'Ready', recommendation: 'Build one deployed API project.' },
    { skill: 'React', readiness: 65, priority: 'Improve', recommendation: 'Create a polished dashboard.' },
    { skill: 'DSA', readiness: 44, priority: 'Critical', recommendation: 'Practice arrays, strings, and trees.' },
    { skill: 'Cloud', readiness: 46, priority: 'Critical', recommendation: 'Deploy one project publicly.' },
  ],
  roadmap: [
    { week: 1, focus: 'Career baseline', actions: ['Finish assessment', 'Pick target roles'], proofOfWork: 'Saved profile' },
    { week: 2, focus: 'Core skill gaps', actions: ['Practice DSA', 'Revise SQL'], proofOfWork: 'Gap score +10' },
    { week: 3, focus: 'Project proof', actions: ['Build feature', 'Deploy demo'], proofOfWork: 'GitHub link' },
    { week: 4, focus: 'Resume sprint', actions: ['Rewrite bullets', 'Add metrics'], proofOfWork: 'ATS 75+' },
  ],
  nextActions: ['Complete week 1', 'Improve DSA', 'Run mock interview'],
};

const demoResume =
  'Java full stack developer with Spring Boot, React, SQL and REST API projects. Built a placement preparation platform and improved dashboard tracking by 30%.';

function App() {
  const [profile, setProfile] = useState({
    name: 'Pavan Sai',
    degree: 'B.Tech CSE',
    targetRole: 'Java Full Stack Developer',
    experienceLevel: 'Student',
    skills: 'Java, Spring Boot, React, SQL, Git',
    interests: 'AI, Web development, Placement preparation',
  });
  const [plan, setPlan] = useState(defaultPlan);
  const [resumeText, setResumeText] = useState(demoResume);
  const [resume, setResume] = useState(null);
  const [interviewAnswer, setInterviewAnswer] = useState(
    'In my recent project, I built REST APIs with Spring Boot and connected them to a React frontend. I owned the dashboard module and improved the demo flow for users.'
  );
  const [feedback, setFeedback] = useState(null);
  const [loading, setLoading] = useState('');

  const modules = useMemo(
    () => [
      { icon: BrainCircuit, label: 'AI Career Profiling', value: `${plan.careerMatches[0]?.fit || 0}% role fit` },
      { icon: Target, label: 'Skill-Gap Matrix', value: `${plan.skillGaps.filter((gap) => gap.readiness < 70).length} gaps found` },
      { icon: Map, label: 'AI Roadmap', value: `${plan.roadmap.length} weeks planned` },
      { icon: FileText, label: 'Resume Intelligence', value: resume ? `${resume.atsScore} ATS` : 'Ready' },
      { icon: MessageSquareText, label: 'Mock Interview AI', value: feedback ? `${feedback.score}/100` : 'Practice' },
      { icon: Gauge, label: 'Live Readiness Score', value: `${plan.readinessScore}/100` },
    ],
    [feedback, plan, resume]
  );

  async function post(path, body, fallback) {
    setLoading(path);
    try {
      const response = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.warn(`Using local demo response for ${path}`, error);
      return fallback;
    } finally {
      setLoading('');
    }
  }

  async function generatePlan() {
    const payload = {
      ...profile,
      skills: profile.skills.split(',').map((skill) => skill.trim()).filter(Boolean),
      interests: profile.interests.split(',').map((interest) => interest.trim()).filter(Boolean),
    };
    const data = await post('/career-plan', payload, defaultPlan);
    setPlan(data);
  }

  async function analyzeResume() {
    const data = await post('/resume/analyze', { targetRole: profile.targetRole, resumeText }, {
      atsScore: 78,
      contentScore: 82,
      impactScore: 74,
      strengths: ['Technical keywords are visible', 'Project ownership is clear'],
      fixes: ['Add more numbers', 'Add deployment link', 'Use stronger action verbs'],
      rewriteSuggestions: [
        'Built a Spring Boot and React platform that improved career readiness tracking with real-time scoring.',
        'Designed REST APIs for assessments, resume analysis, interview feedback, and personalized roadmaps.',
      ],
    });
    setResume(data);
  }

  async function scoreInterview() {
    const data = await post('/interview/score', {
      targetRole: profile.targetRole,
      question: 'Tell me about your best full-stack project.',
      answer: interviewAnswer,
    }, {
      score: 71,
      question: 'Tell me about your best full-stack project.',
      verdict: 'Strong answer. Add one metric and one tradeoff.',
      coachingTips: ['Use STAR structure', 'Mention your exact contribution', 'Close with measurable impact'],
      improvedAnswer: 'I built a full-stack career platform with Spring Boot and React, owned the dashboard and API flow, and delivered a judge-ready demo with scoring, roadmap, resume, and interview modules.',
    });
    setFeedback(data);
  }

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
        <nav>
          {modules.map(({ icon: Icon, label }) => (
            <a href={`#${label.toLowerCase().replaceAll(' ', '-')}`} key={label}>
              <Icon size={18} />
              <span>{label}</span>
            </a>
          ))}
        </nav>
        <div className="sidebar-proof">
          <ShieldCheck size={18} />
          <span>Demo-ready: frontend, backend, APIs, fallback AI, and deployment docs.</span>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Hackathon MVP</p>
            <h1>One platform for assessment, gaps, roadmap, resume, interview, and readiness.</h1>
          </div>
          <button className="primary" onClick={generatePlan} disabled={loading === '/career-plan'}>
            <Rocket size={18} />
            {loading === '/career-plan' ? 'Generating...' : 'Generate AI Plan'}
          </button>
        </header>

        <section className="metrics-grid" aria-label="Career modules">
          {modules.map(({ icon: Icon, label, value }) => (
            <article className="metric-card" key={label} id={label.toLowerCase().replaceAll(' ', '-')}>
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
              <label>Name<input value={profile.name} onChange={(e) => setProfile({ ...profile, name: e.target.value })} /></label>
              <label>Degree<input value={profile.degree} onChange={(e) => setProfile({ ...profile, degree: e.target.value })} /></label>
              <label>Target role<input value={profile.targetRole} onChange={(e) => setProfile({ ...profile, targetRole: e.target.value })} /></label>
              <label>Level<input value={profile.experienceLevel} onChange={(e) => setProfile({ ...profile, experienceLevel: e.target.value })} /></label>
              <label className="wide">Skills<input value={profile.skills} onChange={(e) => setProfile({ ...profile, skills: e.target.value })} /></label>
              <label className="wide">Interests<input value={profile.interests} onChange={(e) => setProfile({ ...profile, interests: e.target.value })} /></label>
            </div>
          </div>

          <div className="panel readiness-panel">
            <div className="score-ring" style={{ '--score': `${plan.readinessScore * 3.6}deg` }}>
              <strong>{plan.readinessScore}</strong>
              <span>Readiness</span>
            </div>
            <div>
              <h2>Top Career Match</h2>
              <h3>{plan.careerMatches[0]?.role}</h3>
              <p>{plan.careerMatches[0]?.reason}</p>
              <div className="match-list">
                {plan.careerMatches.slice(0, 4).map((match) => (
                  <div key={match.role}>
                    <span>{match.role}</span>
                    <meter min="0" max="100" value={match.fit} />
                    <strong>{match.fit}%</strong>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="main-grid">
          <div className="panel">
            <div className="panel-heading">
              <Activity size={21} />
              <h2>Skill-Gap Matrix</h2>
            </div>
            <div className="gap-list">
              {plan.skillGaps.map((gap) => (
                <div className="gap-row" key={gap.skill}>
                  <span>{gap.skill}</span>
                  <div className="heat-track"><i style={{ width: `${gap.readiness}%` }} /></div>
                  <strong className={gap.priority.toLowerCase()}>{gap.priority}</strong>
                </div>
              ))}
            </div>
          </div>

          <div className="panel">
            <div className="panel-heading">
              <Map size={21} />
              <h2>Week-by-Week Roadmap</h2>
            </div>
            <div className="roadmap">
              {plan.roadmap.map((week) => (
                <article key={week.week}>
                  <span>Week {week.week}</span>
                  <strong>{week.focus}</strong>
                  <p>{week.actions.join(' · ')}</p>
                  <small>{week.proofOfWork}</small>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="main-grid">
          <div className="panel">
            <div className="panel-heading">
              <FileText size={21} />
              <h2>Resume Intelligence</h2>
            </div>
            <textarea value={resumeText} onChange={(e) => setResumeText(e.target.value)} />
            <button className="secondary" onClick={analyzeResume} disabled={loading === '/resume/analyze'}>
              <FileText size={17} />
              {loading === '/resume/analyze' ? 'Analyzing...' : 'Analyze Resume'}
            </button>
            {resume && <ScoreSummary scores={[
              ['ATS', resume.atsScore],
              ['Content', resume.contentScore],
              ['Impact', resume.impactScore],
            ]} notes={resume.fixes} />}
          </div>

          <div className="panel">
            <div className="panel-heading">
              <MessageSquareText size={21} />
              <h2>Mock Interview AI</h2>
            </div>
            <p className="question">Tell me about your best full-stack project.</p>
            <textarea value={interviewAnswer} onChange={(e) => setInterviewAnswer(e.target.value)} />
            <button className="secondary" onClick={scoreInterview} disabled={loading === '/interview/score'}>
              <MessageSquareText size={17} />
              {loading === '/interview/score' ? 'Scoring...' : 'Score Answer'}
            </button>
            {feedback && (
              <div className="feedback">
                <strong>{feedback.score}/100</strong>
                <p>{feedback.verdict}</p>
                {feedback.coachingTips.map((tip) => <span key={tip}><CheckCircle2 size={15} />{tip}</span>)}
              </div>
            )}
          </div>
        </section>
      </section>
    </main>
  );
}

function ScoreSummary({ scores, notes }) {
  return (
    <div className="score-summary">
      {scores.map(([label, value]) => (
        <div key={label}>
          <span>{label}</span>
          <strong>{value}</strong>
        </div>
      ))}
      <ul>
        {notes.map((note) => <li key={note}>{note}</li>)}
      </ul>
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App />);
