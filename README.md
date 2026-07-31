# PeerPrep

A platform for students to find peers to practice mock interviews with — pick a topic (DSA, SQL, System Design, HR, etc.), get matched with a compatible partner, schedule a session, and rate each other afterward.

Built with **Spring Boot (Java) + React**, JWT auth, and a custom weighted matching algorithm (not just a plain CRUD list).

## Why this project

Most fresher portfolios have a to-do list or an expense tracker. This is a two-sided matching problem instead — closer to what you'd actually be asked to design at a real company, and the kind of tool a student prepping for placements would genuinely use.

## Architecture

```
peerprep/
├── backend/     Spring Boot REST API (Java 17)
│   └── src/main/java/com/peerprep/
│       ├── model/        JPA entities: User, Skill, UserSkill, Session, Feedback
│       ├── repository/   Spring Data JPA repositories
│       ├── security/     JWT auth (filter, util, Spring Security config)
│       ├── service/      Business logic — MatchingService is the core piece
│       ├── controller/   REST endpoints
│       ├── dto/          Request/response shapes, kept separate from entities
│       └── exception/    Centralized error handling
└── frontend/    React app (Create React App)
    └── src/
        ├── context/      Auth state (JWT stored client-side, attached via axios interceptor)
        ├── pages/        Login, Register, Profile, Dashboard (matching), Sessions
        ├── components/   Reusable UI pieces
        └── services/     API client
```

## The matching algorithm (the part worth explaining in an interview)

`MatchingService.suggestPartners()` scores every candidate who *offers* a topic against the requester who *wants* it, using four normalized (0–1) signals combined with fixed weights:

| Signal | Weight | What it captures |
|---|---|---|
| Proficiency fit | 40% | Rewards a candidate ~1 level above the requester — a realistic "slightly more experienced partner" gap, rather than just matching the highest-rated person to everyone |
| Rating | 25% | Candidate's average post-session rating |
| Availability overlap | 20% | Jaccard similarity between each user's preferred time slots |
| Experience | 15% | Sessions completed, capped at 10 so a few power users don't dominate every result |

Two design decisions worth being able to defend:
- **Cold-start handling**: a brand-new candidate with zero ratings gets a neutral default (3.5/5) instead of 0, so new users aren't buried under people who've just been on the platform longer.
- **Weights are named constants**, not magic numbers, so they're easy to tune and to justify out loud.

Known limitation to mention if asked: it's a greedy weighted-sum ranking, not a stable-matching (Gale–Shapley style) algorithm, so it doesn't guarantee no two users would mutually prefer each other over their current matches. That's a natural "what would you improve" answer.

## Running it locally

**Backend** (needs Java 17 + Maven):
```
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:8080`. Uses an in-memory H2 database — no setup needed, resets on restart. Swap the 4 datasource lines in `application.properties` for MySQL/Postgres if you want persistence.

**Frontend** (needs Node):
```
cd frontend
npm install
npm start
```
Runs on `http://localhost:3000`.

## Trying it out

1. Register two accounts (e.g. `you@test.com` and `friend@test.com`).
2. On account A: Profile → add "DSA" as **WANT**, Beginner.
3. On account B: Profile → add "DSA" as **OFFER**, Intermediate or Advanced.
4. On account A: Dashboard → search "DSA" → you'll see account B ranked with a match score.
5. Request a session, then log in as B to accept it, mark it complete, and leave feedback.

## What's deliberately left out (and why that's fine to say out loud)

- No email/notification system — sessions are pull-based (check the Sessions page), not push. A real v2 would add this.
- No refresh tokens — JWT just expires after 24h and the user re-logs in. Simpler for a portfolio project, and a natural follow-up question ("how would you handle token refresh?") you can answer even though it's not built.
- No pagination on match results — fine at demo scale, would need it with a large user base.

Being upfront about scope decisions like these tends to land better in interviews than pretending the project is production-ready.
