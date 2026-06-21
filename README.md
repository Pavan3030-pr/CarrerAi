# CarrerAi - CareerCompass AI

CareerCompass AI is a hackathon-ready career readiness platform inspired by the supplied presentation. It replaces fragmented placement-prep tools with one workflow: career assessment, skill-gap analysis, AI roadmap, resume intelligence, mock interview feedback, and a live readiness score.

## What is inside

- `frontend/` - React + Vite dashboard with a polished six-module demo flow.
- `backend/` - Java Spring Boot REST API for auth, career plans, resume scoring, and interview feedback.
- `docker-compose.yml` - one-command local container setup.
- `.env.example` - environment variables for Gemini and API URLs.

## Core features

- AI Career Profiling with role-fit scores.
- Skill-Gap Matrix with readiness priorities.
- Week-by-week career roadmap generation.
- Resume Intelligence with ATS, content, and impact scoring.
- Mock Interview AI with coaching feedback.
- Live Readiness Score for placement preparation tracking.

## Run locally

### Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs on [http://localhost:8080](http://localhost:8080).

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on [http://localhost:5173](http://localhost:5173).

## API endpoints

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/career-plan`
- `POST /api/resume/analyze`
- `POST /api/interview/score`

## Demo payload

```json
{
  "name": "Pavan Sai",
  "degree": "B.Tech CSE",
  "targetRole": "Java Full Stack Developer",
  "experienceLevel": "Student",
  "skills": ["Java", "Spring Boot", "React", "SQL", "Git"],
  "interests": ["AI", "Web development", "Placement preparation"]
}
```

## Hackathon pitch

60% of graduates struggle with employability because preparation is fragmented, generic, and hard to measure. CarrerAi turns career readiness into one measurable journey and gives students a clear 8-12 week path to job readiness.

## Future scope

- Real Gemini API client with prompt templates and JSON response parsing.
- PostgreSQL persistence for users, assessments, resumes, interviews, and progress history.
- College admin dashboard for cohort readiness analytics.
- Mobile app and multilingual mentoring.
