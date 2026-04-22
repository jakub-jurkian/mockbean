-- Create table compatible with LangChain4j PgVectorStore default schema
-- It's used vector(768) because nomic-embed-text generates 768-dimensional vectors
CREATE TABLE IF NOT EXISTS embeddings (
    embedding_id UUID PRIMARY KEY,
    embedding vector(768),
    text TEXT NULL,
    metadata JSONB NULL
    );