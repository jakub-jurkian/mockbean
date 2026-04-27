# MockBean — Project Plan

> **Status:** Living document. Updated as decisions are made, not retroactively.
> **Version:** v0.2
> **Last updated:** 2026-04-27
> **Owner:** Jakub Jurkian (kuba.jur03@gmail.com)
> **Started:** 2026-04-21
> **Deadline:** 2026-06-01
> **Scope:** Bachelor-level individual project for *Inteligencja Obliczeniowa*, University of Gdańsk, 2025/26 summer term. Doubles as a personal portfolio piece and a self-use Java interview-prep tool.

---

## Document History

| Date       | Version | Change                                                                                                                                                            |
| ---------- | ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 2026-04-21 | v0.1    | Initial planning sketch in `GUIDE.md` written before first code commit (predecessor to this document)                                                             |
| 2026-04-23 | v0.1.1  | Lightweight checklist updates in `GUIDE.md` after Phase 0 completion                                                                                              |
| 2026-04-27 | v0.2    | `GUIDE.md` reorganized into structured `PROJECT_PLAN.md`, `ARCHITECTURE.md` *(pending)*, and `decisions/` folder for ADRs. Phase 0 retroactively documented from git history. |

---

## 1. Problem Statement

I'm preparing for my first Java Developer role and wanted a project that would push me to learn Java and its production ecosystem deeply — Spring Boot, Postgres, containerization, integration testing — while also producing something I'd actually use myself: a tool that asks me Java interview questions and gives me an honest, structured assessment of my answer.

That dogfooding loop also raises a real research question. Hiring junior Java developers is bottlenecked by the cost of manual technical screening: a senior engineer burns 30–60 minutes per candidate plus context-switching cost. There is increasing interest in using LLMs as automated judges ("LLM-as-a-judge"), but reliability of this approach for entry-level technical screening is unclear — particularly for locally-hosted open-source models versus paid frontier APIs.

**MockBean** is the resulting research prototype: a stateless REST API that asks *can a Retrieval-Augmented LLM act as a reliable judge of Java junior interview answers, and does a free, locally-hosted model close the gap to a paid frontier model?*

---

## 2. Goals & Non-Goals

### Primary goals (academic)
- Build a working, demonstrable REST API that grades free-form answers to Java technical questions.
- Compare ≥3 LLMs as judges across measurable axes: accuracy vs. human ground truth, latency, cost, and gullibility (susceptibility to verbose-but-empty answers).
- Compare two retrieval strategies (Pure Prompt vs. RAG) and three retrieval depths (top-k = 1, 3, 5).
- Produce a written research report (15–25 pages) with a cited bibliography.
- Score 15/15 on the academic grading rubric.

### Secondary goals (personal)
- Use MockBean myself as a Java interview-prep tool while job-hunting.
- Use this project as a portfolio piece signaling Spring Boot, RAG, vector search, and disciplined research.
- Pick up working knowledge of LangChain4j, pgvector, and Ollama I can carry into a real job.

### Non-Goals
- **Not** a production system. No authentication, sessions, multi-tenancy, or CRUD interfaces.
- No frontend UI — Swagger UI is the demo surface.
- No live deployment — local Docker only (deployment is a stretch goal).
- No multi-language support — knowledge base and prompts are English-only.
- No conversational memory — every request is stateless.
- No fine-tuning — prompt engineering and RAG only.

---

## 3. Success Criteria

| #   | Criterion              | Measurable target                                                                   |
| --- | ---------------------- | ----------------------------------------------------------------------------------- |
| SC1 | Academic grade         | 15/15 pts on the course rubric                                                      |
| SC2 | Working demo           | End-to-end demo via Swagger UI runs without errors on a clean `docker-compose up`   |
| SC3 | Adversarial test set   | ≥25 hand-labeled `(question, candidateAnswer, label)` triples                       |
| SC4 | Knowledge base size    | ≥40 Q&A pairs across ≥5 Java topics                                                 |
| SC5 | Models compared        | ≥3 distinct LLMs evaluated end-to-end on the test set                               |
| SC6 | Final report           | 15–25 pages, ≥4 cited papers, charts generated from `evaluation_log` data           |
| SC7 | Code quality           | Layered architecture, Flyway migrations, Testcontainers, ≥1 meaningful integration test, no hardcoded credentials |

---

## 4. Constraints

- **Time:** ~6 weeks total (Apr 21 – Jun 1). Personal cadence: 2–3 h/day, ~15–20 h/week.
- **Team:** Single developer.
- **Hardware:** 32 GB RAM laptop, no dedicated GPU. Constrains which local models can be hosted via Ollama alongside IDE + Postgres.
- **Budget:** $0–20 USD for paid LLM APIs.

---

## 5. High-Level Approach

The system is a stateless REST API.

1. **Ingestion (startup):** `KnowledgeBaseIngestionService` (a `CommandLineRunner`) reads `data/knowledge_base.md`, chunks it, computes embeddings via `nomic-embed-text` on Ollama, and stores them in PostgreSQL with the `pgvector` extension.
2. **Evaluation (per request):** `POST /api/v1/evaluations` embeds the incoming question, runs a similarity search in pgvector to retrieve the ideal reference answer, and prompts the configured LLM ("technical interviewer") with the question, the ideal answer, and the candidate's answer.
3. **Output:** The LLM returns a structured JSON verdict (`score`, `strengths`, `missedConcepts`, `feedback`), mapped via LangChain4j to a Java record.
4. **Logging:** Every evaluation is persisted to `evaluation_log` (with experiment metadata) for offline research analysis.
5. **Research:** Experiments replay a labeled adversarial test set against multiple `(model, strategy, top-k)` combinations; results are analyzed via a Python notebook reading from Postgres.

See [`ARCHITECTURE.md`](./ARCHITECTURE.md) *(pending)* for component diagrams and request-flow details.

---

## 6. Tech Stack & Rationale

| Layer                | Choice                       | Rationale (one-liner; full trade-offs in `decisions/`)              |
| -------------------- | ---------------------------- | ------------------------------------------------------------------- |
| Language / runtime   | Java 21                      | Primary language; aligned with portfolio target (Java junior roles) |
| Web framework        | Spring Boot 3.5              | Industry standard for Java backend                                  |
| LLM orchestration    | LangChain4j 0.29.1           | Native Java; supports Ollama + pgvector + structured output         |
| Local LLM runtime    | Ollama                       | Easy local model hosting; rich model catalog                        |
| Vector store         | pgvector on Postgres 16      | Reuses existing Postgres; HNSW index built-in; one fewer container  |
| Migrations           | Flyway                       | Schema-as-code; reproducible setup                                  |
| Build                | Maven                        | Default for Spring Boot                                             |
| Containers           | Docker Compose               | One-command bring-up of pg + pgvector                               |
| Testing              | JUnit 5 + Testcontainers     | Real Postgres in tests, no DB mocking                               |
| API docs             | Springdoc OpenAPI / Swagger  | Auto-generated from controllers; doubles as the demo UI             |
| Observability        | Spring Boot Actuator         | `/actuator/prometheus` for metrics                                  |

---

## 7. Phased Plan

### Phase 0 — Bootstrap (Apr 21 – Apr 23)

The MVP scaffolding and an end-to-end RAG pipeline. Verified working with a manual smoke test against `POST /api/v1/evaluations` — request goes through ingestion → retrieval → judge → log without errors. **No automated tests yet** — that lands in Phase 1.

- `done` *Apr 21* — `GUIDE.md` written before first code commit (research goals, grading checklist, architecture sketch). Predecessor of this document.
- `done` *Apr 21* — Spring Boot 3.5 + Java 21 skeleton with pgvector and Flyway dependencies (`4f8986f`)
- `done` *Apr 22* — LangChain4j 0.29.1 + local Ollama integration with `llama3.1` chat model and `nomic-embed-text` embeddings (`81dbfdc`)
- `done` *Apr 22* — Initial `data/knowledge_base.md` with Q&A pairs across Core Java, OOP, Collections, Exceptions, and Spring (`1df9769`)
- `done` *Apr 22* — Knowledge base ingestion pipeline: `KnowledgeBaseIngestionService` as a `CommandLineRunner`, recursive document splitter, embedding into pgvector (`28d7a23`)
- `done` *Apr 23* — Domain records (`EvaluationRequest`, `EvaluationResponse`) and `TechnicalInterviewer` LangChain4j AiService interface with structured-output prompt template (`81a93ff`)
- `done` *Apr 23* — `EvaluationService` orchestrating retrieval + judge call; `EvaluationController` exposing `POST /api/v1/evaluations` (`dae7321`)
- `done` *Apr 23* — Persistence layer: `EvaluationLog` JPA entity with JSONB columns for strengths/missedConcepts, Flyway V1/V2 migrations, JPA repository (`d483f6a`)
- `done` *throughout Phase 0* — Disciplined feature-branch workflow: every change merged via PR (`feature/init-infrastructure`, `feature/llm-integration`, `feature/knowledge-base-setup`, `feature/evaluation-engine`)

**Phase 0 retrospective (short):** The biggest win was writing `GUIDE.md` before any code — it kept scope tight when the temptation to over-engineer hit. Biggest debt accumulated: hardcoded credentials in `VectorStoreConfig`, system prompt in Polish (against my own English-only convention), only the trivial `contextLoads()` test, and `AiTestController` left in the codebase as a debug endpoint. All triaged into Phase 1.

### Phase 1 — Foundations & Cleanup (Apr 27 – May 3)

Pay down Phase 0 debt, expand the knowledge base, and add the secondary endpoints needed for the demo.

- `wip` Expand `data/knowledge_base.md` to ≥40 Q&A pairs across ≥5 topics (Core Java, OOP, Collections, Exceptions, Spring, Concurrency)
- `todo` Add `GET /api/v1/topics` and `GET /api/v1/questions/random?topic=...` endpoints
- `todo` Replace hardcoded credentials in `VectorStoreConfig` with `@ConfigurationProperties` (de-duplicate against `application.yaml`)
- `todo` Translate `TechnicalInterviewer` system prompt to English
- `todo` Add `springdoc-openapi-starter-webmvc-ui` for Swagger UI
- `todo` Add `@ControllerAdvice` global exception handler with consistent error response shape
- `todo` Parameterize `top-k` via configuration (`mockbean.retrieval.top-k`)
- `todo` Add `@Valid` and Bean Validation annotations on `EvaluationRequest`
- `todo` Rename / reorganize `AiTestController` package (PascalCase → lowercase) or remove the endpoint
- `todo` First real integration test: spin up Testcontainers Postgres + mocked LLM, hit `/api/v1/evaluations`, assert end-to-end behavior
- `todo` Decide whether `/api/v1/test/ask` ships in the demo (debug-only) — see OQ5

### Phase 2 — Adversarial Test Set & Evaluation Harness (May 4 – May 10)

Build the ground-truth dataset that the rest of the research depends on.

- `todo` Define labeling taxonomy: `correct | partial | wrong | evasive | waffle`
- `todo` Hand-author ≥25 labeled triples in `data/test_set.json`
- `todo` Document the labeling rubric in `docs/METHODOLOGY.md`
- `todo` Build evaluation harness (Python script or Java integration test) that replays the test set against `/evaluate` and writes results
- `todo` Compute baseline accuracy with the current single model (`llama3.1`)
- `todo` (optional) Cross-label ≥10 samples with a peer; compute Cohen's κ for inter-rater agreement

### Phase 3 — Multi-Model Comparison (May 11 – May 17)

- `todo` Resolve OQ1 (final 3-model lineup)
- `todo` Wire up second and third model providers in LangChain4j configuration
- `todo` Re-run the test set across all models
- `todo` Capture latency (p50 / p95) and approximate token cost per evaluation; persist into `evaluation_log`

### Phase 4 — Retrieval Experiments (May 18 – May 24)

- `todo` Add a Pure Prompt mode (RAG retrieval bypassed) toggleable per request
- `todo` Run the matrix: 2 strategies × 3 top-k values × 3 models = 18 experiment runs
- `todo` Add `experiment_id` column to `evaluation_log` (Flyway V3) and tag results
- `todo` Build a Python notebook (`notebooks/analysis.ipynb`) that pulls from Postgres and produces the report charts

### Phase 5 — Report & Presentation (May 25 – May 31)

One-day buffer to 2026-06-01 deadline.

- `todo` Write final report (15–25 pages): abstract, methodology, results, discussion, bibliography
- `todo` Polish `README.md`: hero text, demo GIF, C4 architecture diagram (Mermaid), quickstart
- `todo` Record live demo + prepare screenshot fallback for the defense
- `todo` Practice oral presentation; prepare answers for likely questions
- `todo` Fill in Section 10 (Retrospective) of this document

---

## Appendix A — Working Bibliography

...

## Appendix B — Glossary

- **RAG** — Retrieval-Augmented Generation. Augmenting an LLM prompt with documents retrieved from a knowledge base.
- **LLM-as-a-Judge** — Using a language model to score or compare other model outputs against a reference.
- **HNSW** — Hierarchical Navigable Small World. The graph index `pgvector` uses for approximate nearest-neighbor search.
- **Ground truth** — Reference labels hand-assigned by a human, against which the model's output is scored.
- **Adversarial test set** — A test set deliberately containing tricky cases (verbose-but-empty answers, confidently-worded wrong answers) designed to surface weaknesses.
- **ADR** — Architecture Decision Record. A short, dated note describing a significant design decision, the alternatives considered, and the trade-offs accepted.
