# Learning Studio

An AI-tutor learning platform. This repo is built **feature-by-feature alongside the
GenAI learning path** — each module you master ships the matching feature.

> **Sprint 0 == Module 0.** Setting this project up *is* the Module 0 "setup + hello LLM"
> exercise. The boilerplate is done for you; the LLM calls are yours to implement.

## Stack
- **Backend:** Spring Boot 3.4 (Java 21) + Spring AI · Flyway · PostgreSQL + pgvector
- **Frontend:** Angular (scaffolded in Sprint 1 — target UI is the clickable prototype)
- **Infra:** Docker Compose (Postgres + Redis) · GitHub Actions CI
- **LLM:** BYOK — bring your own key; the app boots fine without one.

## Layout
```
learning-studio/
├─ docker-compose.yml         # Postgres+pgvector + Redis
├─ backend/                   # Spring Boot API
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/studio/
│     │  ├─ config/AuthProvider.java      # seam: current-user resolution
│     │  ├─ config/DevAuthProvider.java   # Sprint 0 stub
│     │  ├─ config/LlmConfig.java         # ChatClient (only when a key is set)
│     │  ├─ web/HealthController.java      # GET /api/health  (works out of the box)
│     │  └─ web/LlmController.java         # GET /api/llm/hello  <-- YOUR Module 0 TODO
│     └─ resources/
│        ├─ application.yml
│        └─ db/migration/V1__init.sql     # schema + seams (org_id, settings, flags)
├─ python/hello_llm.py         # <-- YOUR Module 0 TODO (Python "hello LLM")
└─ frontend/                   # Angular (Sprint 1)
```

## Run it (Sprint 0 acceptance)
```bash
# 1. Start infra
cd learning-studio
docker compose up -d

# 2. Boot the API (no LLM key needed yet)
cd backend
./mvnw spring-boot:run            # uses the bundled Maven wrapper (no local Maven needed)

# 3. Verify — should return {"status":"ok",...} and run the V1 migration
curl http://localhost:8080/api/health
```
✅ **Sprint 0 is done when** `docker compose up` runs, the app boots, `/api/health`
returns ok, and the V1 migration created the tables (check with `psql` or any client).

## Your Module 0 exercises (the learning part)
The tutor left two `TODO`s — do these yourself, they're the point:
1. **Java hello-LLM** — implement the call in `web/LlmController.java`, then:
   ```bash
   export OPENAI_API_KEY=sk-...        # or your provider's key
   # restart the app, then:
   curl "http://localhost:8080/api/llm/hello?prompt=Say%20hi"
   ```
2. **Python hello-LLM** — implement `python/hello_llm.py` and run it.

### Module 0 mastery gate (what your tutor will test)
You pass when you can, without notes:
- Explain what a **token** is and find the **token usage** of your call.
- Explain why the **same prompt** can return **different text**.
- Say when you'd choose **Java (Spring AI)** vs **Python** for LLM work.
- Show **both** hello-LLM apps working.

When ready, tell your tutor: **"Test me on Module 0."**

## What the tutor did vs. what you do
- **Tutor (boilerplate):** repo layout, Docker, DB schema + seams, config, CI, the
  health endpoint, and the `ChatClient` wiring.
- **You (learning):** the two LLM calls, understanding tokens/cost/determinism, and
  passing the gate. That's Module 0.

> Note: this scaffold was generated, not run end-to-end in the tutoring environment —
> running it is *your* Sprint 0 acceptance step. If a dependency version needs a bump,
> check https://start.spring.io and https://spring.io/projects/spring-ai.
