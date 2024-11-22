## Movie Recommendation System

When working with traditional databases, we rely on exact keyword or basic pattern matching to search for DB records. This approach fails to fully understand the context and intent behind natural language queries. Even when using search engines like ElasticSearch or Solr, the engine looks for variants of the literal query without understanding its intent.

Vector stores address this limitation by storing data as numeric vectors that capture its meaning. As a result, similar words end up close to each other, which allows for semantic searching. The conversion of data into vectors is done by an embedding model.

We'll use [pgvector](https://github.com/pgvector/pgvector), an open source PostgreSQL extension, to convert our PostgreSQL database into a vector store.

Our application creates a `movie_plots` table using [Flyway migration scripts](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/movie-recommendation-system/src/main/resources/db/migration). At startup, it fetches random movie records from the [OMDb API](https://www.omdbapi.com/#Usage), converts them to vectors using the [mxbai-embed-large](https://ollama.com/library/mxbai-embed-large) embedding model from Ollama, and stores them in our PostgreSQL table.

A GET `/api/movies/search?query=?` API endpoint is exposed that returns the most relevant movie plots based on the natural language query, even if the plots don't contain the exact keywords used in the query.

## Local Setup

The prerequisites to run the application is an active Docker instance and an [OMDb API key](https://www.omdbapi.com/apikey.aspx) (It's free 😉).

Docker compose can be used to spin up the postgresql database, Ollama service, and the Spring Boot application:

```shell
docker-compose build
```

```shell
docker-compose up -d
```

Alternatively, the Testcontainers for development support can be used to start the application:

```shell
mvn spring-boot:test-run
```
