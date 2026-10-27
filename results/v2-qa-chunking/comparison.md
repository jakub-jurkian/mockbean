# RAG before vs after Q&A-pair chunking

Thresholds: correct >= 0.65, partial >= 0.3. CI: 95% percentile bootstrap, 10,000 resamples, on run 1.

| Model | Mode | Chunking | Accuracy per run | 95% CI (run 1) |
|---|---|---|---|---|
| claude-sonnet-4-6 | RAG | old (300 chars) | 82.5% / 82.5% | 70.0% - 92.5% |
| claude-sonnet-4-6 | RAG | new (Q&A pairs) | 77.5% / 77.5% | 65.0% - 90.0% |
| claude-sonnet-4-6 | PURE | n/a (no retrieval) | 92.5% / 92.5% | 82.5% - 100.0% |
| gemini-flash-2-5 | RAG | old (300 chars) | 80.0% / 80.0% | 67.5% - 92.5% |
| gemini-flash-2-5 | PURE | n/a (no retrieval) | 87.5% / 82.5% | 77.5% - 97.5% |
| llama31 | RAG | old (300 chars) | 50.0% / 50.0% | 35.0% - 65.0% |
| llama31 | RAG | new (Q&A pairs) | 55.0% / 55.0% | 40.0% - 70.0% |
| llama31 | PURE | n/a (no retrieval) | 57.5% / 57.5% | 42.5% - 72.5% |
| llama31 | PURE | same-day control | 57.5% | 42.5% - 72.5% |

## Details

- **claude-sonnet-4-6 · RAG · old (300 chars)** - per label (run 1): correct 87.5%, partial 62.5%, wrong 87.5%, waffle 87.5%, evasive 87.5%
- **claude-sonnet-4-6 · RAG · new (Q&A pairs)** - per label (run 1): correct 87.5%, partial 50.0%, wrong 75.0%, waffle 87.5%, evasive 87.5%
  - retrieval: rag: 80; relevance min 0.832 / mean 0.889
- **claude-sonnet-4-6 · PURE · n/a (no retrieval)** - per label (run 1): correct 87.5%, partial 100.0%, wrong 87.5%, waffle 87.5%, evasive 100.0%
- **gemini-flash-2-5 · RAG · old (300 chars)** - per label (run 1): correct 87.5%, partial 62.5%, wrong 87.5%, waffle 87.5%, evasive 75.0%
- **gemini-flash-2-5 · PURE · n/a (no retrieval)** - per label (run 1): correct 87.5%, partial 87.5%, wrong 87.5%, waffle 87.5%, evasive 87.5%
- **llama31 · RAG · old (300 chars)** - per label (run 1): correct 100.0%, partial 25.0%, wrong 12.5%, waffle 50.0%, evasive 62.5%
- **llama31 · RAG · new (Q&A pairs)** - per label (run 1): correct 100.0%, partial 62.5%, wrong 25.0%, waffle 50.0%, evasive 37.5%
  - retrieval: rag: 80; relevance min 0.832 / mean 0.889
- **llama31 · PURE · n/a (no retrieval)** - per label (run 1): correct 100.0%, partial 62.5%, wrong 25.0%, waffle 62.5%, evasive 37.5%
- **llama31 · PURE · same-day control** - per label (run 1): correct 100.0%, partial 50.0%, wrong 25.0%, waffle 62.5%, evasive 50.0%
