CREATE TABLE IF NOT EXISTS movie_actors (
  movie_id VARCHAR(26) REFERENCES movies(movie_id),
  actor_id VARCHAR(26) REFERENCES actors(actor_id),
  role_name VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (movie_id, actor_id)
); 