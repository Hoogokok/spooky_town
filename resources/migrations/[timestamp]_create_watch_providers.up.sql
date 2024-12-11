CREATE TABLE watch_providers (
  provider_id VARCHAR(26) PRIMARY KEY,
  provider_name VARCHAR(50) NOT NULL UNIQUE,
  logo_url TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO watch_providers (provider_id, provider_name) VALUES
  ('NETFLIX', 'Netflix'),
  ('DISNEY_PLUS', 'Disney Plus'),
  ('WAVVE', 'Wavve'),
  ('GOOGLE_PLAY', 'Google Play'),
  ('TVING', 'TVING'); 