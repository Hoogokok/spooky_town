CREATE TABLE movie_providers (
  movie_id VARCHAR(26) REFERENCES movies(movie_id),
  provider_id VARCHAR(26) REFERENCES watch_providers(provider_id),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (movie_id, provider_id)
); 