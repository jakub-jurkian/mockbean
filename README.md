# MockBean — LLM-as-a-Judge for Java Technical Interviews

> **Research question:** Can a language model reliably replace a human recruiter when evaluating Java developer interview answers?

MockBean is a Spring Boot REST API that uses the **LLM-as-a-Judge** pattern to evaluate candidates' answers to Java interview questions. It compares three language models (Claude Sonnet, Gemini Flash, Llama 3.1) across two evaluation strategies (RAG vs. Pure Prompt) on a hand-crafted adversarial test set of 40 cases.

Built for *Inteligencja Obliczeniowa*, Uniwersytet Gdański, 2025/26.

---

## Key Results

| Model | RAG Accuracy | Pure Prompt Accuracy |
|---|---|---|
| claude-sonnet-4-6 | 82.5% | **92.5%** |
| gemini-2.5-flash | 80.0% | 85.0% |
| llama3.1:8b | 50.0% | 57.5% |

**Surprising finding:** Pure Prompt outperforms RAG for all three models. For a domain well-covered by pre-training data (Core Java), retrieved context introduces *partial-match bias* rather than improving evaluation quality.

---

## Architecture

```
Client (Python harness / curl / Swagger)
        │
        ▼
EvaluationController  (POST /api/v1/evaluations)
        │
        ▼
EvaluationService
    ├── RAG mode:
    │       nomic-embed-text  ──►  pgvector (top-1 similarity)
    │                                    │
    │                               idealAnswer  ──►  LLM Judge
    └── Pure Prompt mode:
            (no retrieval)  ──────────────────►  LLM Judge
                                                      │
                                                      ▼
                                             EvaluationResponse (JSON)
                                             + EvaluationLog (PostgreSQL)
```

**Tech stack:** Java 21 · Spring Boot 3.5 · LangChain4j 0.29.1 · PostgreSQL + pgvector · Flyway · Ollama · Swagger UI

**Model switching** is handled entirely through Spring profiles — no code changes required:
- Default profile → Llama 3.1:8b (via Ollama, local)
- `claude-sonnet-4-6` profile → Claude Sonnet 4.6 (Anthropic API)
- `gemini-flash` profile → Gemini 2.5 Flash (Google API)

---

## Quick Start

**Prerequisites:** Docker, Java 21, Maven, Ollama (for Llama)

**1. Start the database**
```bash
docker-compose up -d
```

**2. Run the application**

With Llama (local, default):
```bash
# Pull the model first
ollama pull llama3.1
ollama pull nomic-embed-text

./mvnw spring-boot:run
```

With Claude Sonnet:
```bash
export ANTHROPIC_API_KEY=your_key_here
./mvnw spring-boot:run -Dspring-boot.run.profiles=claude-sonnet-4-6
```

With Gemini Flash:
```bash
export GEMINI_API_KEY=your_key_here
./mvnw spring-boot:run -Dspring-boot.run.profiles=gemini-flash
```

**3. Open Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

---

## API

### Evaluate a candidate answer

```http
POST /api/v1/evaluations
Content-Type: application/json

{
  "question": "What is the difference between == and equals()?",
  "userAnswer": "== compares references, equals() compares values.",
  "mode": "rag"
}
```

`mode` is either `"rag"` (retrieves a reference answer from pgvector) or `"pure"` (model judges from its own knowledge only).

**Response:**
```json
{
  "score": 0.85,
  "strengths": ["Correctly distinguishes reference vs. value comparison"],
  "missedConcepts": ["equals() contract with hashCode()", "String pool behaviour"],
  "feedback": "Good foundational answer. Missing deeper nuances expected at junior level."
}
```

Score mapping: `≥ 0.65` → correct · `0.30–0.65` → partial · `< 0.30` → low

---

## Reproducing the Experiments

Install Python dependencies:
```bash
pip install -r scripts/requirements.txt
```

Run a single experiment (app must be running with the correct profile):
```bash
python scripts/run_evaluation.py --model claude-sonnet-4-6 --mode pure
python scripts/run_evaluation.py --model claude-sonnet-4-6 --mode rag
python scripts/run_evaluation.py --model gemini-flash --mode pure
python scripts/run_evaluation.py --model llama3.1 --mode rag
```

Compute Bootstrap 95% CI and threshold sensitivity analysis on existing results:
```bash
python scripts/bootstrap_analysis.py
```

Results are saved under `results/` as CSV files.

---

## Project Structure

```
mockbean/
├── data/
│   ├── knowledge_base.md       # 50 Q&A pairs (7 topics) loaded into pgvector at startup
│   └── test_set.json           # 40 adversarial test cases (5 labels × 8 each)
├── results/
│   ├── claude-sonnet-4-6/      # 4 CSV files (RAG/Pure × Run1/Run2)
│   ├── gemini-flash-2-5/       # 4 CSV files
│   └── llama31/                # 4 CSV files
├── scripts/
│   ├── run_evaluation.py       # Evaluation harness — calls the API, saves CSVs
│   ├── bootstrap_analysis.py   # Bootstrap CI + threshold sensitivity analysis
│   └── baseline_cosine.py      # Non-LLM baseline: TF-IDF + cosine similarity
├── raport/
│   ├── main.tex                # LaTeX research report (Polish)
│   └── bibliografia.bib        # BibTeX references
├── src/main/java/com/mockbean/
│   ├── ai/TechnicalInterviewer.java      # LangChain4j AI service (RAG + Pure prompts)
│   ├── service/EvaluationService.java    # Core evaluation logic + vector retrieval
│   ├── config/                           # Spring profiles for each model
│   └── domain/                           # Request/Response/Log DTOs
└── docker-compose.yml          # PostgreSQL + pgvector
```

---

## Test Set Design

The 40 test cases are distributed equally across five answer quality labels:

| Label | Description | Count |
|---|---|---|
| `correct` | Accurate and complete answer | 8 |
| `partial` | Partially correct, lacks depth | 8 |
| `wrong` | Contains factual errors | 8 |
| `waffle` | Sounds plausible but is technically empty | 8 |
| `evasive` | Candidate admits not knowing | 8 |

Labels `wrong`, `waffle`, and `evasive` form the **adversarial group** — answers that should score low but may fool a lenient model. The `waffle` and `evasive` cases also feed the **gullibility metric** (fraction of adversarial answers scored ≥ 0.5).

---

## Licence

Academic project — Uniwersytet Gdański, 2025/26.
