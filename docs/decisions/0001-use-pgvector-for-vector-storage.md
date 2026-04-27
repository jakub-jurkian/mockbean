# ADR 0001: Use pgvector for vector storage

## Status

Accepted — 2026-04-21

> Documented retroactively on 2026-04-27, during the v0.2 reorganization
> of `GUIDE.md` into structured planning docs. The decision itself was
> made during initial scaffolding on 2026-04-21 (commit `4f8986f`).

## Context
The project requires a vector store to support Retrieval-Augmented
Generation (RAG): embedded knowledge-base entries must be searchable
by semantic similarity. Practical options were:

- Dedicated vector databases: Qdrant, Weaviate, Milvus
- Postgres extensions: pgvector
- In-memory libraries: FAISS, Chroma in-memory mode

Constraints at decision time: solo developer, ~5-week timeline,
knowledge base expected to stay under 100 Q&A pairs, and the project
already required PostgreSQL for the `evaluation_log` table.

## Decision
Use pgvector running in the same Postgres instance that hosts
the relational data.

## Consequences

### Positive
- One container in `docker-compose` instead of two — simpler dev
  setup and reproducibility for graders.
- Single source of truth: `evaluation_log` and embeddings live in the
  same DB; future analytical joins are trivial.
- Spring Boot has first-class Postgres support (Flyway, JPA,
  `@DataJpaTest`) — fewer moving parts.
- LangChain4j ships a ready-made `langchain4j-pgvector` adapter,
  removing integration code.
- HNSW indexing is supported natively, which maps cleanly onto the
  "beyond lecture material" section of the final report.

### Negative
- Less feature-rich than Qdrant (no payload filters, no native
  clustering, no horizontal scaling).
- Performance ceiling is lower than dedicated vector DBs above
  ~1M vectors — irrelevant at this project's scale, noted for
  future scaling.
- Requires installing the `vector` extension via Flyway migration
  (`V2__create_embeddings_table.sql`).

## Alternatives Considered

- **Qdrant** — best-in-class performance and filtering, but adds a
  second container, a second client library, and a steeper learning
  curve. Not justified for ≤100 vectors.
- **In-memory FAISS** — simplest possible setup, but loses all data
  on restart. Would force re-ingestion on every container boot,
  hurting iteration speed during development.
- **Weaviate** — schema-driven and powerful, but heavyweight for this
  scale; separate auth model adds friction without value here.