CREATE TABLE theaters (
  theater_id VARCHAR(26) PRIMARY KEY,  -- ULID
  uuid UUID NOT NULL UNIQUE,
  chain_type VARCHAR(20) NOT NULL,     -- 'cgv', 'megabox', 'lotte'
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_theaters_uuid ON theaters(uuid);
CREATE INDEX idx_theaters_chain_type ON theaters(chain_type);

CREATE TABLE movie_theaters (
  movie_id VARCHAR(26) REFERENCES movies(movie_id),
  theater_id VARCHAR(26) REFERENCES theaters(theater_id),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (movie_id, theater_id)
);

CREATE INDEX idx_movie_theaters_theater_id ON movie_theaters(theater_id); 