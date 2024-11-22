CREATE TABLE movie_plots (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1024)
);

CREATE INDEX ON movie_plots USING HNSW (embedding vector_cosine_ops);